package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.CashedNetworkTimeDistance;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.matsim.av.passenger.AVRequest;

public class GlobalBipartiteMatchingCached {

    private final CashedNetworkTimeDistance distanceCashed;

    public GlobalBipartiteMatchingCached(CashedNetworkTimeDistance distanceCashed) {
        this.distanceCashed = distanceCashed;
    }

    protected Map<RoboTaxi, AVRequest> match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, double now) {
        GlobalBipartiteWeight specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                Scalar dist = distanceCashed.distance(roboTaxi.getDivertableLocation(), link, now);
                return dist.number().doubleValue();
            }
        };
        return GlobalBipartiteHelper.genericMatch(roboTaxis, requests, AVRequest::getFromLink, specificWeight);
    }

}
