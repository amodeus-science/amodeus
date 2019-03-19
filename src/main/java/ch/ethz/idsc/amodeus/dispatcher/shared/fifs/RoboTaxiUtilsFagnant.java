/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum RoboTaxiUtilsFagnant {
    ;

    /** Finds all the robotaxis which can reach a specific link within the maximal Time specified
     * 
     * @param link
     * @param robotaxis
     * @param timeDb
     * @param maxTime
     * @return */
    static NavigableMap<Double, RoboTaxi> getRoboTaxisWithinMaxTime( //
            Link link, Collection<RoboTaxi> robotaxis, NetworkTimeDistInterface timeDb, double maxTime, //
            RoboTaxiHandler roboTaxiMaintainer, Double now) {
        Collection<RoboTaxi> closeRoboTaxis = roboTaxiMaintainer.getRoboTaxisWithinFreeSpeedDisk(link.getCoord(), maxTime).stream().filter(rt -> robotaxis.contains(rt))
                .collect(Collectors.toSet());
        NavigableMap<Double, RoboTaxi> map = new TreeMap<>();
        for (RoboTaxi roboTaxi : closeRoboTaxis) {
            double travelTimeToLink = timeDb.travelTime(link, roboTaxi.getDivertableLocation(), now).number().doubleValue();
            if (travelTimeToLink < maxTime) {
                map.put(travelTimeToLink, roboTaxi);
            }
        }
        return map;
    }

    /** Finds the closest RoboTaxi to the from Link of theAvRequest out of all RoboTaxis
     * in the {@link unassignedRoboTaxis} Set. Thereby the maximal drive time in
     * the network can not be exceeded.
     * The Optional RoboTaxi is present if there exists a Robotaxi in the set which
     * can reach the AV Request location within {@link maxTime}. It is Empty if no RoboTaxi
     * in the Set fulfills this constraint.
     * 
     * @param roboTaxiMaintainer
     * @param avRequest
     * @param maxTime
     * @param now
     * @param timeDb
     * @return */
    static Optional<RoboTaxi> getClosestUnassignedRoboTaxiWithinMaxTime( //
            RoboTaxiHandler roboTaxiMaintainer, AVRequest avRequest, double maxTime, double now, NetworkTimeDistInterface timeDb) {
        NavigableMap<Double, RoboTaxi> roboTaxis = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), roboTaxiMaintainer.getUnassignedRoboTaxis(), timeDb,
                maxTime, roboTaxiMaintainer,now);
        if (roboTaxis.isEmpty()) {
            return Optional.empty();
        }
        GlobalAssert.that(roboTaxis.firstKey() < maxTime);
        GlobalAssert.that(timeDb.travelTime(avRequest.getFromLink(), roboTaxis.firstEntry().getValue().getDivertableLocation(), now).number().doubleValue() < maxTime);
        return Optional.of(roboTaxis.firstEntry().getValue());
    }
}
