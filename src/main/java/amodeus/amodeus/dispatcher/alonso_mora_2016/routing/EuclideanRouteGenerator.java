package amodeus.amodeus.dispatcher.alonso_mora_2016.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;

import amodeus.amodeus.dispatcher.alonso_mora_2016.routing.DefaultTravelFunction.PartialSolution;

public class EuclideanRouteGenerator implements RouteGenerator {
    private final Link startLink;
    private final List<Link> locations;

    private final int numberOfDirectives;

    private PartialSolution partial;
    private final List<Integer> alternatives = new LinkedList<>();

    private final boolean failEarly;
    private boolean expanded = true;

    public EuclideanRouteGenerator(boolean failEarly, Link startLink, List<Link> locations, double now, int initialPassengers) {
        this.startLink = startLink;

        this.numberOfDirectives = locations.size();
        this.locations = locations;

        this.partial = new PartialSolution(Arrays.asList(), -1, now, 0.0, initialPassengers);

        for (int i = 0; i < numberOfDirectives; i++) {
            alternatives.add(i);
        }

        this.failEarly = failEarly;
        this.expanded = true;
    }

    @Override
    public PartialSolution next() {
        Link originLink = partial.indices.size() > 0 ? locations.get(partial.indices.get(partial.indices.size() - 1)) : startLink;

        double minimumDistance = Double.POSITIVE_INFINITY;
        int minimumDistanceIndex = -1;

        for (int alternativeIndex : alternatives) {
            Link destinationLink = locations.get(alternativeIndex);

            double distance = CoordUtils.calcEuclideanDistance(originLink.getCoord(), destinationLink.getCoord());

            if (distance < minimumDistance) {
                minimumDistance = distance;
                minimumDistanceIndex = alternativeIndex;
            }
        }

        Iterator<Integer> indexIterator = alternatives.iterator();

        while (indexIterator.hasNext()) {
            if (indexIterator.next() == minimumDistanceIndex) {
                indexIterator.remove(); // We cannot call remove because integer is ambiguous
            }
        }

        expanded = false;

        return new PartialSolution(partial.indices, minimumDistanceIndex, partial.time, partial.cost, partial.passengers);
    }

    @Override
    public void expand(PartialSolution partial, double updatedTime, int updatedPassengers, double updatedCost) {
        List<Integer> updatedIndices = new ArrayList<>(numberOfDirectives);
        updatedIndices.addAll(partial.indices);
        updatedIndices.add(partial.addedIndex);

        this.partial = new PartialSolution(updatedIndices, -1, updatedTime, updatedCost, updatedPassengers);
        alternatives.clear();

        for (int index = 0; index < numberOfDirectives; index++) {
            if (!updatedIndices.contains(index)) {
                alternatives.add(index);
            }
        }

        expanded = true;
    }

    @Override
    public boolean hasNext() {
        if (alternatives.size() > 0) {
            if (failEarly && !expanded) {
                return false;
            }

            return true;
        }

        return false;
    }

    static public void main(String[] args) {
        Link link1 = new MockLink(0.0);
        Link link2 = new MockLink(100.0);
        Link link3 = new MockLink(200.0);
        Link link4 = new MockLink(300.0);
        Link link5 = new MockLink(400.0);
        Link link6 = new MockLink(400.0);

        List<Link> locations = Arrays.asList(link4, link5, link6);
        EuclideanRouteGenerator generator = new EuclideanRouteGenerator(true, link1, locations, 0.0, 0);

        while (generator.hasNext()) {
            PartialSolution solution = generator.next();
            System.out.println(solution.indices + " " + solution.addedIndex);

            if (solution.addedIndex == 1) {
                generator.expand(solution, 0, 0, 0);
            }

            if (solution.addedIndex == 2) {
                generator.expand(solution, 0, 0, 0);
            }
        }
    }

    static public class MockLink implements Link {
        private final Coord coord;

        public MockLink(double x) {
            this.coord = new Coord(x, 0.0);
        }

        @Override
        public Coord getCoord() {
            return coord;
        }

        @Override
        public Attributes getAttributes() {
            return null;
        }

        @Override
        public Id<Link> getId() {
            return null;
        }

        @Override
        public boolean setFromNode(Node node) {
            return true;
        }

        @Override
        public boolean setToNode(Node node) {
            return true;
        }

        @Override
        public Node getToNode() {
            return null;
        }

        @Override
        public Node getFromNode() {
            return null;
        }

        @Override
        public double getLength() {
            return 0;
        }

        @Override
        public double getNumberOfLanes() {
            return 0;
        }

        @Override
        public double getNumberOfLanes(double time) {
            return 0;
        }

        @Override
        public double getFreespeed() {
            return 0;
        }

        @Override
        public double getFreespeed(double time) {
            return 0;
        }

        @Override
        public double getCapacity() {
            return 0;
        }

        @Override
        public double getCapacity(double time) {
            return 0;
        }

        @Override
        public void setFreespeed(double freespeed) {

        }

        @Override
        public void setLength(double length) {

        }

        @Override
        public void setNumberOfLanes(double lanes) {

        }

        @Override
        public void setCapacity(double capacity) {
            // TODO Auto-generated method stub

        }

        @Override
        public void setAllowedModes(Set<String> modes) {
            // TODO Auto-generated method stub

        }

        @Override
        public Set<String> getAllowedModes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public double getCapacityPeriod() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getFlowCapacityPerSec() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getFlowCapacityPerSec(double time) {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
