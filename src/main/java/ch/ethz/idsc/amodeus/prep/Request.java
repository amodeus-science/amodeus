/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.sca.Clip;

/** This class represents a trip request where the departureTime, the departureLink and the destinationLink are stored */
public class Request {
    private static final Clip TIME_CLIP = Clip.function(0, 24 * 3600 - 1);

    /** returns the requests that have their startLink in the given set of links */
    public static Set<Request> filterLinks(Set<Request> requests, Set<Link> links) {
        return requests.stream().filter(request -> links.contains(request.startLink)).collect(Collectors.toSet());
    }

    private final double startTime;
    private final Link startLink;
    private final Link endLink;

    public Request(double startTime, Link startLink, Link endLink) {
        TIME_CLIP.requireInside(RealScalar.of(startTime));
        this.startTime = startTime;
        this.startLink = startLink;
        this.endLink = endLink;
    }

    public double startTime() {
        return startTime;
    }

    public Link startLink() {
        return startLink;
    }

    public Link endLink() {
        return endLink;
    }
}
