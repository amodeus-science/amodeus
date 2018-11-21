/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package*/ enum RoboTaxiUtilsFagnant {
    ;

    /** Finds all the robotaxis which can reach a specific link within the maximal Time specified
     * 
     * @param link
     * @param robotaxis
     * @param timeDb
     * @param maxTime
     * @return */
    /* package */ static NavigableMap<Double, RoboTaxi> getRoboTaxisWithinMaxTime(Link link, Set<RoboTaxi> robotaxis, LeastCostCalculatorDatabaseOneTime timeDb, double maxTime) {
        NavigableMap<Double, RoboTaxi> map = new TreeMap<>();
        for (RoboTaxi roboTaxi : robotaxis) {
            double travelTimeToLink = timeDb.timeFromTo(link, roboTaxi.getDivertableLocation()).number().doubleValue();
            if (travelTimeToLink < maxTime) {
                map.put(travelTimeToLink, roboTaxi);
            }
        }
        return map;
    }

    /** Finds the closest RoboTaxi to the from Link of theAvRequest out of all RoboTaxis in the {@link unassignedRoboTaxis} Set. Thereby the maximal drive time in
     * the network can not be exceeded.
     * The Optional RoboTaxi is present if there exists a Robotaxi in the set which can reach the AV Request location within {@link maxTime}. It is Empty if no Robo
     * Taxi in the Set fulfills this constraint.
     * 
     * @param unassignedRoboTaxis
     * @param avRequest
     * @param maxTime
     * @param now
     * @param timeDb
     * @return */
    /* package */ static Optional<RoboTaxi> getClosestRoboTaxiWithinMaxTime(Set<RoboTaxi> unassignedRoboTaxis, AVRequest avRequest, double maxTime, double now,
            LeastCostCalculatorDatabaseOneTime timeDb) {
        NavigableMap<Double, RoboTaxi> roboTaxis = RoboTaxiUtilsFagnant.getRoboTaxisWithinMaxTime(avRequest.getFromLink(), unassignedRoboTaxis, timeDb, maxTime);
        if (roboTaxis.isEmpty()) {
            return Optional.empty();
        }
        GlobalAssert.that(roboTaxis.firstKey() < maxTime);
        GlobalAssert.that(timeDb.timeFromTo(avRequest.getFromLink(), roboTaxis.firstEntry().getValue().getDivertableLocation()).number().doubleValue() < maxTime);
        return Optional.of(roboTaxis.firstEntry().getValue());
    }
}
