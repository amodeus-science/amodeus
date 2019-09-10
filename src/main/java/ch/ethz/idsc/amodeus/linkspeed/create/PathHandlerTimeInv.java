/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.routing.CachedNetworkPropertyComputation;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Total;

/* package */ class PathHandlerTimeInv {

    public final Scalar duration;
    public final Scalar freeflowDuation;
    public final List<Link> travelledLinks = new ArrayList<>();

    public PathHandlerTimeInv(TaxiTrip taxiTrip, CachedNetworkPropertyComputation<NavigableMap<Double, Link>> travelTimeCalc, //
            AmodeusTimeConvert timeConvert, ClosestLinkSelect linkSelect, //
            LocalDate simulationDate) {

        PathHandlerUtil.validityCheck(taxiTrip, timeConvert, simulationDate, linkSelect);

        /** extract data from {@link TaxiTrip} */
        this.duration = taxiTrip.duration;

        /** extract origin, destination and start time */
        Link pickupLink = linkSelect.linkFromWGS84(taxiTrip.pickupLoc);
        Link dropOffLink = linkSelect.linkFromWGS84(taxiTrip.dropoffLoc);
        int tripStart = timeConvert.ldtToAmodeus(taxiTrip.pickupDate, simulationDate);

        /** compute shortest path */
        NavigableMap<Double, Link> linkEntryTimes = travelTimeCalc.fromTo(pickupLink, dropOffLink, tripStart);
        linkEntryTimes.values().forEach(l -> travelledLinks.add(l));

        /** compute free flow duration and travelled links */
        Tensor times = Tensors.empty();
        travelledLinks.stream()//
                .map(l -> Quantity.of(l.getLength() / l.getFreespeed(), "s"))//
                .forEach(q -> times.append(q));
        freeflowDuation = (Scalar) Total.of(times);
    }
}
