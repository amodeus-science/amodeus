package amodeus.amodeus.dispatcher.core.schedule;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

public class FutureVrpPathWithTravelData implements VrpPathWithTravelData {
    private VrpPathWithTravelData delegate = null;

    private final Link fromLink;
    private final Link toLink;
    private final double departureTime;
    private final double estimatedArrivalTime;
    private final Future<Path> pathFuture;
    private final TravelTime travelTime;

    public FutureVrpPathWithTravelData(double departureTime, double estimatedArrivalTime, Link fromLink, Link toLink,
            Future<Path> pathFuture, TravelTime travelTime) {
        this.fromLink = fromLink;
        this.toLink = toLink;
        this.departureTime = departureTime;
        this.pathFuture = pathFuture;
        this.travelTime = travelTime;
        this.estimatedArrivalTime = estimatedArrivalTime;
    }

    private void prepareDelegate() {
        if (delegate == null) {
            try {
                Path path = pathFuture.get();
                delegate = VrpPaths.createPath(fromLink, toLink, departureTime, path, travelTime);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public int getLinkCount() {
        prepareDelegate();
        return delegate.getLinkCount();
    }

    @Override
    public Link getLink(int idx) {
        prepareDelegate();
        return delegate.getLink(idx);
    }

    @Override
    public double getLinkTravelTime(int idx) {
        prepareDelegate();
        return delegate.getLinkTravelTime(idx);
    }

    @Override
    public void setLinkTravelTime(int idx, double linkTT) {
        prepareDelegate();
        delegate.setLinkTravelTime(idx, linkTT);
    }

    @Override
    public Link getFromLink() {
        return fromLink;
    }

    @Override
    public Link getToLink() {
        return toLink;
    }

    @Override
    public Iterator<Link> iterator() {
        prepareDelegate();
        return delegate.iterator();
    }

    @Override
    public double getDepartureTime() {
        return departureTime;
    }

    @Override
    public double getTravelTime() {
        return getArrivalTime() - getDepartureTime();
    }

    @Override
    public double getArrivalTime() {
        if (delegate == null) {
            return estimatedArrivalTime;
        } else {
            return delegate.getArrivalTime();
        }
    }

    @Override
    public VrpPathWithTravelData withDepartureTime(double timeShift) {
        // TODO Auto-generated method stub
        return null;
    }
}
