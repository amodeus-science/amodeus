/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityFromNetworkAndIdentifier extends ParkingCapacityAdapter {

    /** Reads the spatial capacities from the Network file.
     * If the given tag for the spatialCapacity is found its
     * corresponding value is taken for the AV Spatial
     * Capacity.
     * 
     * @param network
     * @param spatialAvCapacityString */
    public ParkingCapacityFromNetworkAndIdentifier(Network network, String spatialAvCapacityString) {
        for (Link link : network.getLinks().values()) {
            Long spots = (Long) link.getAttributes().getAttribute(spatialAvCapacityString);

            if (spots != null && spots > 0)
                capacities.put(link.getId(), spots);
        }
        waringNoCapacities(spatialAvCapacityString);
    }

    private void waringNoCapacities(String spatialAvCapacityString) {
        // Check that there exist Links with parking Spaces. Otherwise this makes not much sense
        if (capacities.isEmpty()) {
            System.err.println("Watch out:");
            System.err.println("There exists no capacity limits in the network for the String " + spatialAvCapacityString + ".");
            System.err.println("Assuming this is deliberately and thus continuing with the simulation");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
