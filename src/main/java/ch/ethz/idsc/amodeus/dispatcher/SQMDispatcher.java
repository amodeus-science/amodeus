/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.PartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** Implementation of the SQM algorithm (pp. 5625) of "Fundamental Performance
 * Limits and Efficient Polices for Transportation-On-Demand Systems" presented
 * by M.Pavone, K.Treleaven, E.Frazzoli, 2010, 49th IEEE Conference on Decision
 * and Control, pp.5622-5629
 * 
 * Upon arrival, a demand is assigned to the depot closest to its pick-up
 * location. The RoboTaxi services its demands in FIFO order returning to the
 * depot after each delivery, and waiting there if its queue is empty.
 * 
 * The number of vehicles and virtual nodes have to be equal.
 * 
 * @author fluric */
public class SQMDispatcher extends PartitionedDispatcher {
    private final Map<VirtualNode<Link>, UnitCapRoboTaxi> nodeToTaxi = new HashMap<>();
    private final Map<UnitCapRoboTaxi, VirtualNode<Link>> taxiToNode = new HashMap<>();
    private final Map<VirtualNode<Link>, Link> nodeToLink = new HashMap<>();
    private final List<Link> virtualCenters;
    private final FastLinkLookup fastLinkLookup;

    protected SQMDispatcher(//
            Config config, //
            AVDispatcherConfig avconfig, //
            TravelTime travelTime, //
            AVGeneratorConfig generatorConfig, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            Network network, //
            VirtualNetwork<Link> virtualNetwork) {
        super(config, avconfig, travelTime, router, eventsManager, virtualNetwork);
        GlobalAssert.that(virtualNetwork != null);
        GlobalAssert.that((int) generatorConfig.getNumberOfVehicles() == virtualNetwork.getvNodesCount());
        this.fastLinkLookup = new FastLinkLookup(network, MatsimStaticDatabase.INSTANCE);
        this.virtualCenters = assignNodesToNearestLinks(virtualNetwork.getVirtualNodes());
    }

    @Override
    public void redispatch(double now) {
        // skip redispatch at the beginning where RoboTaxis are not yet initialized
        if (getRoboTaxis().isEmpty()) {
            return;
        }

        // for the first time assign to each virtual node the closest link
        if (nodeToTaxi.isEmpty()) {
            assignVirtualNodes();
        }

        List<AVRequest> unassigned_requests = getUnassignedAVRequests();

        for (UnitCapRoboTaxi taxi : getRoboTaxiSubset(RoboTaxiStatus.STAY)) {

            // move unassigned taxis back to their virtual station
            if (taxi.getDivertableLocation() != nodeToLink.get(taxiToNode.get(taxi))) {
                setRoboTaxiRebalance(taxi, nodeToLink.get(taxiToNode.get(taxi)));
            }

            // assign pick-up demands to the according taxi in the virtualStation in a
            // first-in first-out manner
            else {
                double earliestSubmission = Double.MAX_VALUE;
                AVRequest earliestAvr = null;
                for (AVRequest avr : unassigned_requests) {
                    if (taxiToNode.get(taxi).getLinks().contains(avr.getFromLink()) && avr.getSubmissionTime() < earliestSubmission) {
                        earliestSubmission = avr.getSubmissionTime();
                        earliestAvr = avr;
                    }
                }
                if (earliestAvr != null) {
                    setRoboTaxiPickup(taxi, earliestAvr);
                }
            }
        }
    }

    private void assignVirtualNodes() {
        Collection<VirtualNode<Link>> nodes = virtualNetwork.getVirtualNodes();
        List<UnitCapRoboTaxi> taxis = getRoboTaxis();
        GlobalAssert.that(nodes.size() == taxis.size());
        GlobalAssert.that(nodeToTaxi.isEmpty() && taxiToNode.isEmpty());

        int i = 0;
        for (VirtualNode<Link> node : nodes) {
            nodeToTaxi.put(node, taxis.get(i));
            taxiToNode.put(taxis.get(i), node);
            nodeToLink.put(node, virtualCenters.get(i));
            i++;
        }

        GlobalAssert.that(nodeToTaxi.size() == nodes.size() && taxiToNode.size() == taxis.size());
    }

    /** Returns the nearest {@link Link}'s to the according {@link VirtualNode}'s.
     * Using fastLinkLookup
     * 
     * @param nodes
     *            {@link Collection} of {@link VirtualNode}'s which are the virtual
     *            station
     * @return nearestLinks {@link ArrayList} of {@link Link}'s which are the
     *         closest links to corresponding virtual nodes
     * @author fluric */
    private List<Link> assignNodesToNearestLinks(Collection<VirtualNode<Link>> nodes) {
        List<Link> list = new ArrayList<>();

        for (VirtualNode<Link> node : nodes) {
            // get the center coordinate

            Coord coord = TensorCoords.toCoord(node.getCoord());

            // find the closest link
            int index = fastLinkLookup.getLinkFromXY(coord);
            Link closest = MatsimStaticDatabase.INSTANCE.getOsmLink(index).link;

            list.add(closest);
        }

        return list;
    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator router;

        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject
        private Network network;
        public static VirtualNetwork<Link> virtualNetwork;

        @Inject
        private Config config;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            try {
                virtualNetwork = VirtualNetworkGet.readDefault(network);
            } catch (IOException e) {
                e.printStackTrace();
                GlobalAssert.that(false);
            }
            return new SQMDispatcher(config, avconfig, travelTime, generatorConfig, router, eventsManager, network, //
                    virtualNetwork);
        }
    }

}