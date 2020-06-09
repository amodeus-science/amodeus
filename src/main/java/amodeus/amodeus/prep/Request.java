/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

/** This class represents a trip request where the departureTime, the departureLink and the destinationLink are stored */
public class Request {
    /** returns the requests that have their startLink in the given set of links */
    public static Set<Request> filterLinks(Set<Request> requests, Set<Link> links) {
        return requests.stream() //
                .filter(request -> links.contains(request.startLink)) //
                .collect(Collectors.toSet());
    }

    // ---
    private final double startTime;
    private final Link startLink;
    private final Link endLink;

    public Request(double startTime, Link startLink, Link endLink) {
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
