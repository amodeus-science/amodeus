/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum EuclideanDistanceFunction implements DistanceFunction {
    INSTANCE;

    @Override
    public double getDistance(RoboTaxi robotaxi, AVRequest avrequest) {
        return CoordUtils.calcEuclideanDistance(robotaxi.getDivertableLocation().getCoord(), avrequest.getFromLink().getCoord());
    }

    @Override
    public double getDistance(RoboTaxi robotaxi, Link link) {
        return CoordUtils.calcEuclideanDistance(robotaxi.getDivertableLocation().getCoord(), link.getCoord());
    }

    @Override
    public double getDistance(Link from, Link to) {
        return CoordUtils.calcEuclideanDistance(from.getCoord(), to.getCoord());
    }

}
