/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.Collection;
import java.util.SortedMap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedTimeSeries;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;

/* package */ enum MeanLinkSpeed {
    ;

    /** @return mean speed for a {@link Link} @param link with neighboring {@link Link}s @param neighbors
     *         at a certain @param time. The rationale of the approach is that the reduction / increase of the link
     *         speed is the sage as the average of its neighbors. */
    public static Scalar neighborAverage(Collection<Link> neighbors, Integer time, Link link, //
            MatsimAmodeusDatabase db, LinkSpeedDataContainer lsData) {
        Tensor changes = Tensors.empty();
        for (Link neighbor : neighbors) {
            /** retrieve the link speed estimate of the neighbor */
            int idNeighbor = db.getLinkIndex(neighbor);
            SortedMap<Integer, LinkSpeedTimeSeries> neighborMap = lsData.getLinkSet();
            LinkSpeedTimeSeries series = neighborMap.get(idNeighbor);
            GlobalAssert.that(time >= 0);
            try {
                Tensor tensor = series.getSpeedsAt(time);
                Scalar mean = (Scalar) Mean.of(tensor);
                double actual = mean.number().doubleValue();
                double freeFlow = neighbor.getFreespeed();
                double change = actual / freeFlow;
                changes.append(RealScalar.of(change));
            } catch (Exception exception) {
                // TODO this catch is necessary because the class LinkSpeedTimeSeries
                // is not able to say if any recordings were made without creating an exception,
                // fix eventually...
                // --
            }
        }
        Scalar meanChange = changes.length() == 0 ? //
                RealScalar.ONE : (Scalar) Mean.of(changes);
        return RealScalar.of(link.getFreespeed()).multiply(meanChange);
    }
}
