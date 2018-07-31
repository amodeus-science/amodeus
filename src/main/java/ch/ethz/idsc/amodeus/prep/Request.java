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

    /* package */ Request(double startTime, Link startLink) {
        this.startTime = startTime;
        this.startLink = startLink;
    }

    public double startTime() {
        return startTime;
    }

    public Link startLink() {
        return startLink;
    }
}
