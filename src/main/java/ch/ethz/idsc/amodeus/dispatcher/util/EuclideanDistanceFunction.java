/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public class EuclideanDistanceFunction implements DistanceFunction {
    @Override
    public final double getDistance(UnitCapRoboTaxi robotaxi, AVRequest avrequest) {
        return CoordUtils.calcEuclideanDistance(robotaxi.getDivertableLocation().getCoord(), avrequest.getFromLink().getCoord());
    }

    @Override
    public final double getDistance(UnitCapRoboTaxi robotaxi, Link link) {
        return CoordUtils.calcEuclideanDistance(robotaxi.getDivertableLocation().getCoord(), link.getCoord());

    }

    @Override
    public final double getDistance(Link from, Link to) {
        return CoordUtils.calcEuclideanDistance(from.getCoord(), to.getCoord());
    }

}
