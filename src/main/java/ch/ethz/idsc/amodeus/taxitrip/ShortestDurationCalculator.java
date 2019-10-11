package ch.ethz.idsc.amodeus.taxitrip;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ShortestDurationCalculator {

    private final FastLinkLookup fll;
    private final LeastCostPathCalculator lcpc;

    public ShortestDurationCalculator(Network network, MatsimAmodeusDatabase db) {
        lcpc = new FastAStarLandmarksFactory().createPathCalculator(network, //
                new TravelDisutility() { // free speed travel time
                    @Override
                    public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                        return getLinkMinimumTravelDisutility(link);
                    }

                    @Override
                    public double getLinkMinimumTravelDisutility(Link link) {
                        return link.getLength() / link.getFreespeed();
                    }
                }, //
                new TravelTime() {
                    @Override
                    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
                        return link.getLength() / link.getFreespeed();
                    }
                });
        // fast link lookup
        fll = new FastLinkLookup(network, db);
    }

    public ShortestDurationCalculator(LeastCostPathCalculator lcpc, Network network, MatsimAmodeusDatabase db) {
        this.lcpc = lcpc;
        // fast link lookup
        fll = new FastLinkLookup(network, db);
    }

    public Scalar computeFreeFlowTime(TaxiTrip taxiTrip) {
        return Quantity.of(computePath(taxiTrip).travelTime, "s");
    }

    public Path computePath(TaxiTrip taxiTrip) {
        Link pickupLink = fll.getLinkFromWGS84(TensorCoords.toCoord(taxiTrip.pickupLoc));
        Link dropOffLink = fll.getLinkFromWGS84(TensorCoords.toCoord(taxiTrip.dropoffLoc));
        return lcpc.calcLeastCostPath(pickupLink.getFromNode(), dropOffLink.getToNode(), 1, null, null);
    }
}
