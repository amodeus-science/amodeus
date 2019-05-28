/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.matsim.av.passenger.AVRequest;

/** perform a global bipartite matching of {@link RoboTaxi} and {@link AVRequest}
 * or {@link Link} using the Hungarian Method */
@Deprecated // TODO don-t use, will be deleted by Claudio after BipartiteMatching code refactor
public class GlobalBipartiteMatching2 extends AbstractRoboTaxiDestMatcher {

    private final DistanceFunction distanceFunction;
    // private final ILPGlobalBipartiteHelper glpHelper;
    private double alpha;
    private double beta;
    private double gamma;

    public GlobalBipartiteMatching2(DistanceFunction distanceFunction, double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
        this.distanceFunction = distanceFunction;
        
    }
    
    public GlobalBipartiteMatching2(DistanceFunction distanceFunction) {
        alpha = 1;
        beta = 0.15;
        gamma = 0.8;
        this.distanceFunction = distanceFunction;
        // this.glpHelper = new ILPGlobalBipartiteHelper(new GLPKAssignmentSolver());
    }

    @Override
    protected Map<RoboTaxi, AVRequest> protected_match(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests) {
        GlobalBipartiteWeight specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                return distanceFunction.getDistance(roboTaxi, link);
            }
        };
        return (new GlobalBipartiteHelperILP<AVRequest>(new GLPKAssignmentSolverBetter()))//
                .genericMatch(roboTaxis, requests, AVRequest::getFromLink, specificWeight);
    }

    @Override
    protected Map<RoboTaxi, Link> protected_matchLink(Collection<RoboTaxi> roboTaxis, Collection<Link> links) {
        GlobalBipartiteWeight specificWeight = new GlobalBipartiteWeight() {
            @Override
            public double between(RoboTaxi roboTaxi, Link link) {
                return distanceFunction.getDistance(roboTaxi, link);
            }
        };
        return null;
//        return (new ILPGlobalBipartiteHelper(new GLPKAssignmentSolverBetter(alpha,beta,gamma)))//
//                .genericMatch(roboTaxis, links, link -> link, specificWeight);
    }
}
