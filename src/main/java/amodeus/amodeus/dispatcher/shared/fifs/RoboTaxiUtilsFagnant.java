/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.routing.NetworkTimeDistInterface;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ enum RoboTaxiUtilsFagnant {
    ;

    /** Finds all the {@link RoboTaxi}s which can reach a specific link within the maximal Time specified
     * 
     * @param link
     * @param roboTaxis
     * @param timeDb
     * @param maxTime
     * @return */
    public static NavigableMap<Double, RoboTaxi> getRoboTaxisWithinMaxTime( //
            Link link, Collection<RoboTaxi> roboTaxis, NetworkTimeDistInterface timeDb, double maxTime, //
            RoboTaxiHandler roboTaxiMaintainer, double now) {
        Collection<RoboTaxi> closeRoboTaxis = roboTaxiMaintainer.getRoboTaxisWithinFreeSpeedDisk(link.getCoord(), maxTime).stream().filter(roboTaxis::contains) //
                .collect(Collectors.toSet());
        NavigableMap<Double, RoboTaxi> map = new TreeMap<>();
        for (RoboTaxi roboTaxi : closeRoboTaxis) {
            double travelTimeToLink = timeDb.travelTime(link, roboTaxi.getDivertableLocation(), now).number().doubleValue();
            if (travelTimeToLink < maxTime)
                map.put(travelTimeToLink, roboTaxi);
        }
        return map;
    }

    /** Finds the closest RoboTaxi to the from Link of theAvRequest out of all RoboTaxis
     * in the {@link RoboTaxiHandler#getUnassignedRoboTaxis} Set. Thereby the maximal drive time in
     * the network can not be exceeded.
     * The Optional RoboTaxi is present if there exists a Robotaxi in the set which
     * can reach the AV Request location within {@param maxTime}. It is Empty if no RoboTaxi
     * in the Set fulfills this constraint.
     * 
     * @param roboTaxiHandler
     * @param avRequest
     * @param maxTime
     * @param now
     * @param timeDb
     * @return */
    public static Optional<RoboTaxi> getClosestUnassignedRoboTaxiWithinMaxTime( //
            RoboTaxiHandler roboTaxiHandler, PassengerRequest avRequest, double maxTime, double now, NetworkTimeDistInterface timeDb) {
        NavigableMap<Double, RoboTaxi> roboTaxis = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), roboTaxiHandler.getUnassignedRoboTaxis(), timeDb,
                maxTime, roboTaxiHandler, now);
        if (roboTaxis.isEmpty()) {
            return Optional.empty();
        }
        GlobalAssert.that(roboTaxis.firstKey() < maxTime);
        GlobalAssert.that(timeDb.travelTime(avRequest.getFromLink(), roboTaxis.firstEntry().getValue().getDivertableLocation(), now).number().doubleValue() < maxTime);
        return Optional.of(roboTaxis.firstEntry().getValue());
    }
}
