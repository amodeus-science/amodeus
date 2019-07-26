package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.concurrent.TimeUnit;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

/* package */ class ParkingCapacityFromNetwork extends ParkingCapacityAbstract {

    public ParkingCapacityFromNetwork(Network network, String string, double limit) {
        Long totCap = (long) 0;
        Long totLinks = (long) 0;
        for (Link link : network.getLinks().values()) {

            Long spots = (long) 0;
            try {
                spots = (long) Math.floor(limit * Long.valueOf(link.getAttributes().getAttribute(string).toString()));
                // if (!(spots >= 2)) {
                // spots = (long) 0;
                // }
            } catch (Exception e) {
                // ---
            }

            capacities.put(link.getId(), spots);
            totCap += spots;

            if (spots > 0) {
                totLinks += 1;
            }
        }

        warningNoCap(string);

        System.out.println("Total Parking Capacity: " + totCap.toString());
        System.out.println("Total Parking Links: " + totLinks.toString());
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            System.out.println("no waiting");
        }

    }

    private void warningNoCap(String string) {
        if (capacities.isEmpty()) {
            System.err.println("Watch out:");
            System.err.println("There exists no capacity limits in the network for the String " + string + ".");
            System.err.println("Assuming this is deliberately and thus continuing with the simulation");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
