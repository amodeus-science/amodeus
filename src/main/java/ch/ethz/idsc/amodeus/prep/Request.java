package ch.ethz.idsc.amodeus.prep;

import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

public class Request {
    public static Set<Request> filterLinks(Set<Request> requests, Set<Link> links) {
        return requests.stream().filter(request -> links.contains(request.startLink)).collect(Collectors.toSet());
    }

    private final double startTime;
    private final Link startLink;
    private final Link endLink;

    /* package */ Request(double startTime, Link startLink, Link endLink) {
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
