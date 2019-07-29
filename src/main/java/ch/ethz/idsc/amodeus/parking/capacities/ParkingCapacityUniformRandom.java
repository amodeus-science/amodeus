package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityUniformRandom extends ParkingCapacityAbstract {

    // TODO rebuild to allow not only 2 but n spaces per link
    public ParkingCapacityUniformRandom(Network network, long totSpaces, Random random) {

        /** get links */
        List<Id<Link>> allLinks = new ArrayList<>();
        for (Link link : network.getLinks().values()) {
            allLinks.add(link.getId());
        }

        /** shuffle list */
        Collections.shuffle(allLinks, random);

        /** assign 2 spaces to every link */
        long remainingSpaces = totSpaces;
        int i = 1;
        while (remainingSpaces != 0) {
            capacities.put(allLinks.get(i), (long) 2);
            remainingSpaces -= 2;
            i++;
        }

    }

}
