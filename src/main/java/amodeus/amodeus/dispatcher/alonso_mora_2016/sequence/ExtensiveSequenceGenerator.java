package amodeus.amodeus.dispatcher.alonso_mora_2016.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import com.google.common.base.Optional;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/** Provides all possible combiations of pickup-dropoff sequences for a given list of existing points.
 * 
 * Based on Heap's algorithm at https://en.wikipedia.org/wiki/Heap%27s_algorithm
 * 
 * @author sebhoerl */
public class ExtensiveSequenceGenerator implements Iterator<List<StopDirective>> {
    private final List<StopDirective> directives;

    private final int[] existingIndices;
    private final int[] pickupIndices;
    private final int[] dropoffIndices;
    private final int[] sequenceIndices;

    private final int numberOfExistingRequests;
    private final int numberOfAddedRequests;
    private final int numberOfDirectives;

    private Optional<List<StopDirective>> nextDirectives;

    private int[] c;
    private int i = 0;

    ExtensiveSequenceGenerator(List<PassengerRequest> existingRequests, List<PassengerRequest> addedRequests) {
        // Obtain counts
        this.numberOfAddedRequests = addedRequests.size();
        this.numberOfExistingRequests = existingRequests.size();
        this.numberOfDirectives = numberOfExistingRequests + numberOfAddedRequests * 2;

        // Set up initial feasible directive list
        directives = new ArrayList<>(numberOfExistingRequests + numberOfAddedRequests * 2);
        existingRequests.forEach(r -> directives.add(Directive.dropoff(r)));
        addedRequests.forEach(r -> directives.add(Directive.pickup(r)));
        addedRequests.forEach(r -> directives.add(Directive.dropoff(r)));

        this.existingIndices = new int[numberOfExistingRequests];
        this.pickupIndices = new int[numberOfAddedRequests];
        this.dropoffIndices = new int[numberOfAddedRequests];
        this.sequenceIndices = new int[numberOfExistingRequests + numberOfAddedRequests * 2];

        for (int i = 0; i < numberOfExistingRequests; i++) {
            existingIndices[i] = i;
        }

        for (int i = 0; i < numberOfAddedRequests; i++) {
            pickupIndices[i] = numberOfExistingRequests + i;
            dropoffIndices[i] = numberOfExistingRequests + numberOfAddedRequests + i;
        }

        for (int i = 0; i < numberOfExistingRequests + numberOfAddedRequests * 2; i++) {
            sequenceIndices[i] = i;
        }

        this.c = new int[numberOfDirectives];
        this.nextDirectives = internalNext(true);
    }

    private boolean isPickupIndex(int index) {
        return index >= numberOfExistingRequests && index < numberOfExistingRequests + numberOfAddedRequests;
    }

    private int getPickupIndex(int index) {
        return index - numberOfExistingRequests;
    }

    private boolean isDropoffIndex(int index) {
        return index >= numberOfExistingRequests + numberOfAddedRequests;
    }

    private int getDropoffIndex(int index) {
        return index - numberOfExistingRequests - numberOfAddedRequests;
    }

    @Override
    public boolean hasNext() {
        return nextDirectives.isPresent();
    }

    @Override
    public List<StopDirective> next() {
        List<StopDirective> returnValue = nextDirectives.get();
        nextDirectives = internalNext(false);
        return returnValue;
    }

    private Optional<List<StopDirective>> internalNext(boolean isInitial) {
        if (isInitial) {
            return Optional.of(construct());
        }

        while (i < numberOfDirectives) {
            if (c[i] < i) {
                if (i % 2 == 0) {
                    swap(0, i);
                } else {
                    swap(c[i], i);
                }

                c[i] += 1;
                i = 0;

                if (isValid()) {
                    return Optional.of(construct());
                }
            } else {
                c[i] = 0;
                i += 1;
            }
        }

        return Optional.absent();
    }

    private void swap(int indexA, int indexB) {
        int directiveA = sequenceIndices[indexA];
        int directiveB = sequenceIndices[indexB];

        sequenceIndices[indexB] = directiveA;
        sequenceIndices[indexA] = directiveB;

        if (isPickupIndex(directiveA)) {
            pickupIndices[getPickupIndex(directiveA)] = indexB;
        } else if (isDropoffIndex(directiveA)) {
            dropoffIndices[getDropoffIndex(directiveA)] = indexB;
        }

        if (isPickupIndex(directiveB)) {
            pickupIndices[getPickupIndex(directiveB)] = indexA;
        } else if (isDropoffIndex(directiveB)) {
            dropoffIndices[getDropoffIndex(directiveB)] = indexA;
        }
    }

    private boolean isValid() {
        for (int i = 0; i < numberOfAddedRequests; i++) {
            if (pickupIndices[i] >= dropoffIndices[i]) {
                return false;
            }
        }

        return true;
    }

    private List<StopDirective> construct() {
        List<StopDirective> proposal = new ArrayList<>(numberOfDirectives);

        for (int i = 0; i < numberOfDirectives; i++) {
            proposal.add(directives.get(sequenceIndices[i]));
        }

        return proposal;
    }

    static public class Factory implements SequenceGeneratorFactory {
        @Override
        public Iterator<List<StopDirective>> create(Link startLink, List<PassengerRequest> existingRequests, List<PassengerRequest> addedRequests) {
            return new ExtensiveSequenceGenerator(existingRequests, addedRequests);
        }
    }

    /** TODO: Below here to be transformed into a unit test */

    static public class MockRequest implements PassengerRequest {
        private final Id<Request> requestId;

        public MockRequest(Id<Request> requestId) {
            this.requestId = requestId;
        }

        @Override
        public double getSubmissionTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Id<Request> getId() {
            return requestId;
        }

        @Override
        public double getEarliestStartTime() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Link getFromLink() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Link getToLink() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Id<Person> getPassengerId() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getMode() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    static public void main(String[] args) {
        MockRequest request1 = new MockRequest(Id.create(1, Request.class));
        MockRequest request2 = new MockRequest(Id.create(2, Request.class));
        MockRequest request3 = new MockRequest(Id.create(3, Request.class));
        MockRequest request4 = new MockRequest(Id.create(4, Request.class));

        ExtensiveSequenceGenerator generator = new ExtensiveSequenceGenerator(Arrays.asList(), Arrays.asList(request1, request2));

        while (generator.hasNext()) {
            List<StopDirective> candidate = generator.next();
            List<String> output = new LinkedList<>();

            for (StopDirective directive : candidate) {
                output.add(String.format("%s[%s]", directive.isPickup() ? "P" : "D", directive.getRequest().getId()));
            }

            System.out.println(String.join(" ", output));
        }
    }
}
