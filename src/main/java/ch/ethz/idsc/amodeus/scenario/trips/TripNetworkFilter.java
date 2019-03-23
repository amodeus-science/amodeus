/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.trips;

import java.util.stream.Stream;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.scenario.dataclean.DataFilter;

public class TripNetworkFilter implements DataFilter<TaxiTrip> {

    public Stream<TaxiTrip> filter(Stream<TaxiTrip> stream, ScenarioOptions simOptions, Network network) {
        ReferenceFrame rf = simOptions.getLocationSpec().referenceFrame();
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        Coord minCoord = rf.coords_toWGS84().transform(new Coord(networkBounds[0], networkBounds[1]));
        Coord maxCoord = rf.coords_toWGS84().transform(new Coord(networkBounds[2], networkBounds[3]));
        if (minCoord.equals(new Coord(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)) && //
                maxCoord.equals(new Coord(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY))) {
            System.err.println("WARN network seems to be empty.");
            return stream;
        }
        return stream.filter(trip -> //
        inBounds(minCoord, maxCoord, trip.pickupLoc) && inBounds(minCoord, maxCoord, trip.dropoffLoc));
    }

    private boolean inBounds(Coord minCoord, Coord maxCoord, Coord loc) {
        return (loc.getX() >= minCoord.getX() && loc.getX() <= maxCoord.getX() && // in x Coord
                loc.getY() >= minCoord.getY() && loc.getY() <= maxCoord.getY()); // in y Coord
    }

}
