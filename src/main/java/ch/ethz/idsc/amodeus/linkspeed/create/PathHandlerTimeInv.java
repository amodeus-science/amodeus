/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.taxitrip.ShortestDurationCalculator;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class PathHandlerTimeInv {

    public final Scalar duration;
    public final Scalar freeflowDuation;
    public final List<Link> travelledLinks = new ArrayList<>();
    private final boolean isValid;

    public PathHandlerTimeInv(TaxiTrip taxiTrip, ShortestDurationCalculator calc) {

        /** compute fastest path */
        Path fastest = calc.computePath(taxiTrip);

        /** extract data from {@link TaxiTrip} */
        this.duration = taxiTrip.duration;
        /** extract data from free flow shortest path */
        this.freeflowDuation = Quantity.of(fastest.travelTime, "s");

        fastest.links.forEach(l -> travelledLinks.add(l));

        isValid = Scalars.lessEquals(freeflowDuation, duration);

    }

    public boolean isValid() {
        return isValid;
    }

}
