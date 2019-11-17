/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.vehicles.VehicleType;

import com.google.inject.Inject;

import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Floor;
import ch.ethz.idsc.tensor.sca.Sign;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.generator.AVUtils;

/** class generates {@link AVVehicle}s. It takes the required minimal initial vehicle numbers from
 * {@link TravelData}. In each virtual station it places the required number of vehicles.
 * Within the virtual station a random link is chosen as initial destination.
 * If the minimal required vehicle numbers are reached,
 * the rest of the vehicles is distributed randomly among the virtual stations.
 * If no distribution is given, an equal distribution is chosen.
 * 
 * CLASS NAME IS USED AS IDENTIFIER - DO NOT RENAME CLASS */
public class VehicleToVSGenerator implements AVGenerator {
    private static final long DEFAULT_RANDOM_SEED = 4711;
    // ---
    private final VirtualNetwork<Link> virtualNetwork;
    private final Tensor vehicleDistribution;
    private final Random random;
    private final VehicleType vehicleType;
    private final OperatorConfig operatorConfig;
    // ---
    protected Tensor placedVehicles;

    public VehicleToVSGenerator(OperatorConfig operatorConfig, //
            VirtualNetwork<Link> virtualNetwork, //
            TravelData travelData, VehicleType vehicleType) {
        this.operatorConfig = operatorConfig;
        this.virtualNetwork = virtualNetwork;
        this.vehicleType = vehicleType;

        /** get distribution from travelData */
        Tensor v0 = travelData.getV0();
        GlobalAssert.that(Total.of(v0).Get().number().intValue() <= operatorConfig.getGeneratorConfig().getNumberOfVehicles());

        boolean noDistribution = Scalars.isZero(Total.of(v0).Get());
        long average = operatorConfig.getGeneratorConfig().getNumberOfVehicles() / virtualNetwork.getvNodesCount();
        int vNodes = virtualNetwork.getvNodesCount();

        vehicleDistribution = noDistribution ? Tensors.vector(v -> RealScalar.of(average), vNodes) : Floor.of(v0);
        placedVehicles = Array.zeros(vNodes);

        /** make sure that {@link Random} is reset every subsequent simulation */
        random = new Random(DEFAULT_RANDOM_SEED);
    }

    @Override
    public List<AVVehicle> generateVehicles() {
        int generatedNumberOfVehicles = 0;
        List<AVVehicle> vehicles = new LinkedList<>();
        placedVehicles = Array.zeros(virtualNetwork.getvNodesCount());

        while (generatedNumberOfVehicles < operatorConfig.getGeneratorConfig().getNumberOfVehicles()) {
            ++generatedNumberOfVehicles;
            int vNodeIndex = getNextVirtualNode();
            Link linkGen = getNextLink(virtualNetwork.getVirtualNode(vNodeIndex));
            /** update placedVehicles */
            placedVehicles.set(RealScalar.ONE::add, vNodeIndex);
            Id<DvrpVehicle> id = AVUtils.createId(operatorConfig.getId(), generatedNumberOfVehicles);
            vehicles.add(new AVVehicle(id, linkGen, 0.0, Double.POSITIVE_INFINITY, vehicleType));
        }
        return vehicles;
    }

    /** Returns the index of the first virtual station that is still in need of more vehicles. If all virtual stations have enough, return random index */
    private int getNextVirtualNode() {
        for (int i = 0; i < virtualNetwork.getvNodesCount(); i++)
            if (Sign.isPositive(vehicleDistribution.Get(i).subtract(placedVehicles.Get(i))))
                return i;
        return random.nextInt(virtualNetwork.getvNodesCount());
    }

    /** Return a random {@link Link} of the according virtual station with index vNodeIndex */
    protected Link getNextLink(VirtualNode<Link> vNode) {
        Collection<Link> links = vNode.getLinks();
        List<Link> sortedLinks = SortedLinks.of(links); /** needed for identical outcome with a certain random seed */
        int elemRand = random.nextInt(links.size());
        return sortedLinks.get(elemRand);
    }

    /** for debugging */
    public Tensor getPlacedVehicles() {
        return placedVehicles;
    }

    public static class Factory implements AVGenerator.AVGeneratorFactory {
        @Inject
        private Map<Id<AVOperator>, TravelData> travelDatas;

        @Inject
        private Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks;

        @Override
        public AVGenerator createGenerator(OperatorConfig operatorConfig, Network network, VehicleType vehicleType) {
            return new VehicleToVSGenerator(operatorConfig, virtualNetworks.get(operatorConfig.getId()), travelDatas.get(operatorConfig.getId()), vehicleType);
        }
    }
}
