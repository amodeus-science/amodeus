/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.TypeLiteral;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.PartitionedDispatcher;
import amodeus.amodeus.net.FastLinkLookup;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.TensorCoords;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;

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
    private final MatsimAmodeusDatabase db;
    private final Map<VirtualNode<Link>, RoboTaxi> nodeToTaxi = new HashMap<>();
    private final Map<RoboTaxi, VirtualNode<Link>> taxiToNode = new HashMap<>();
    private final Map<VirtualNode<Link>, Link> nodeToLink = new HashMap<>();
    private final List<Link> virtualCenters;
    private final FastLinkLookup fastLinkLookup;

    protected SQMDispatcher(Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, //
            EventsManager eventsManager, Network network, //
            VirtualNetwork<Link> virtualNetwork, MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db, rebalancingStrategy, RoboTaxiUsageType.SINGLEUSED);
        // <- virtualNetwork is non-null here
        GlobalAssert.that(operatorConfig.getGeneratorConfig().getNumberOfVehicles() == virtualNetwork.getvNodesCount());
        this.db = db;
        this.fastLinkLookup = new FastLinkLookup(network, db);
        this.virtualCenters = assignNodesToNearestLinks(virtualNetwork.getVirtualNodes());
    }

    @Override
    public void redispatch(double now) {
        // skip redispatch at the beginning where RoboTaxis are not yet initialized
        if (getRoboTaxis().isEmpty())
            return;

        // for the first time assign to each virtual node the closest link
        if (nodeToTaxi.isEmpty())
            assignVirtualNodes();

        List<PassengerRequest> unassigned_requests = new ArrayList<>(getUnassignedRequests());

        for (RoboTaxi taxi : getRoboTaxiSubset(RoboTaxiStatus.STAY)) {
            // move unassigned taxis back to their virtual station
            if (taxi.getDivertableLocation() != nodeToLink.get(taxiToNode.get(taxi)))
                setRoboTaxiRebalance(taxi, nodeToLink.get(taxiToNode.get(taxi)));

            // assign pick-up demands to the according taxi in the virtualStation in a
            // first-in first-out manner
            else {
                double earliestSubmission = Double.MAX_VALUE;
                PassengerRequest earliestAvr = null;
                for (PassengerRequest avr : unassigned_requests) {
                    if (taxiToNode.get(taxi).getLinks().contains(avr.getFromLink()) && avr.getSubmissionTime() < earliestSubmission) {
                        earliestSubmission = avr.getSubmissionTime();
                        earliestAvr = avr;
                    }
                }
                if (Objects.nonNull(earliestAvr))
                    setRoboTaxiPickup(taxi, earliestAvr, Double.NaN, Double.NaN);
            }
        }
    }

    private void assignVirtualNodes() {
        Collection<VirtualNode<Link>> nodes = virtualNetwork.getVirtualNodes();
        List<RoboTaxi> taxis = getRoboTaxis();
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
        return nodes.stream().map(VirtualNode::getCoord).map(TensorCoords::toCoord) // get the center coordinate
                .mapToInt(fastLinkLookup::indexFromLocal).mapToObj(db::getOsmLink).map(osml -> osml.link) // find the closest link
                .collect(Collectors.toList());
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            VirtualNetwork<Link> virtualNetwork = inject.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
            });
            
            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            return new SQMDispatcher(config, operatorConfig, travelTime, router, eventsManager, network, //
                    virtualNetwork, db, rebalancingStrategy);
        }
    }

}