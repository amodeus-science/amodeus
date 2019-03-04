/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.idsc.amodeus.routing.CashedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.EasyMinDistPathCalculator;
import ch.ethz.idsc.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.tensor.Scalar;

/* package */ enum StaticHelper {
    ;

    public static double getEuclideanTripDistance(TaxiTrip trip) {
        return CoordUtils.calcEuclideanDistance(trip.pickupLoc, trip.dropoffLoc);
    }

    public static Scalar getMinNetworkTripDistance(TaxiTrip trip, Network network) {
        CashedNetworkTimeDistance lcpc = new CashedNetworkTimeDistance//
        (EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()), 180000.0, TimeDistanceProperty.INSTANCE);
        // find links
        Link linkStart = NetworkUtils.getNearestLink(network, trip.pickupLoc);
        Link linkEnd = NetworkUtils.getNearestLink(network, trip.dropoffLoc);
        // shortest path
        return lcpc.distance(linkStart, linkEnd);
    }

    public static boolean sameDay(LocalDateTime date1, LocalDateTime date2) {
        return date1.getYear() == date2.getYear() && //
                date1.getMonth() == date2.getMonth() && //
                date1.getDayOfMonth() == date2.getDayOfMonth();
    }

}
