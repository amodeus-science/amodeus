package amodeus.amodeus.dispatcher.alonso_mora_2016.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraParameters;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.alonso_mora_2016.TravelTimeCalculator;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class ExtensiveTravelFunction implements AlonsoMoraTravelFunction {
    private final double now;
    private final TravelTimeCalculator travelTimeCalculator;
    private final AlonsoMoraParameters parameters;

    private final double pickupDuration;
    private final double dropoffDuration;

    private final IdMap<Request, AlonsoMoraRequest> requests;

    public ExtensiveTravelFunction(AlonsoMoraParameters parameters, double now, TravelTimeCalculator travelTimeCalculator, IdMap<Request, AlonsoMoraRequest> requests,
            double pickupDuration, double dropoffDuration) {
        this.now = now;
        this.travelTimeCalculator = travelTimeCalculator;
        this.parameters = parameters;
        this.requests = requests;
        this.pickupDuration = pickupDuration;
        this.dropoffDuration = dropoffDuration;
    }

    private Optional<Result> optimize(List<AlonsoMoraRequest> proposedRequests, Optional<AlonsoMoraVehicle> vehicle) {
        // Prepare vehicle information
        int capacity = Integer.MAX_VALUE;
        List<Link> startLinks = new LinkedList<>();
        List<AlonsoMoraRequest> onboardRequests = Collections.emptyList();

        if (vehicle.isPresent()) {
            capacity = vehicle.get().getCapacity();
            onboardRequests = getExistingRequests(vehicle.get());
            startLinks.add(vehicle.get().getLocation());
        } else {
            for (AlonsoMoraRequest request : proposedRequests) {
                startLinks.add(request.getRequest().getFromLink());
            }
        }

        // Prepare directives and indices
        int numberOfExistingDirectives = onboardRequests.size();
        int numberOfProposedDirectives = proposedRequests.size() * 2;
        int numberOfDirectives = numberOfExistingDirectives + numberOfProposedDirectives;

        List<StopDirective> directives = new ArrayList<>(numberOfDirectives);
        List<AlonsoMoraRequest> associatedRequests = new ArrayList<>(numberOfDirectives);

        for (AlonsoMoraRequest request : onboardRequests) {
            directives.add(Directive.dropoff(request.getRequest()));
            associatedRequests.add(request);
        }

        for (AlonsoMoraRequest request : proposedRequests) {
            directives.add(Directive.pickup(request.getRequest()));
            associatedRequests.add(request);
        }

        for (AlonsoMoraRequest request : proposedRequests) {
            directives.add(Directive.dropoff(request.getRequest()));
            associatedRequests.add(request);
        }

        // Prepare optimization queue
        double minimumCost = Double.POSITIVE_INFINITY;
        Result minimumCostSolution = null;
        int numberOfSolutions = 0;

        for (Link startLink : startLinks) {
            List<PartialSolution> queue = new LinkedList<>();
            queue.add(new PartialSolution(Arrays.asList(), now, 0.0));

            while (queue.size() > 0 && numberOfSolutions < parameters.routeOptimizationLimit) {
                PartialSolution partial = queue.remove(queue.size() - 1); // Depth first

                // TODO: Switch order here. Here, we draw from the stack, create all potential
                // solutions and evalate them. Better would be to evaluate one proposed solution
                // that is drawn from the stack, evaluating it, and adding child solutions if
                // the current one is feasible!

                // Find indices all that can be added
                List<Integer> indices = new ArrayList<>(partial.indices.size() + 1);

                for (int i = 0; i < numberOfDirectives; i++) {
                    if (!partial.indices.contains(i)) {
                        indices.add(i);
                    }
                }

                // Advance existing solutions
                for (int index : indices) {
                    // First, we need to check if the added index does not introduce a pickup after a dropoff
                    StopDirective addedDirective = directives.get(index);

                    if (addedDirective.isPickup()) {
                        for (int pastIndex : partial.indices) {
                            StopDirective pastDirective = directives.get(pastIndex);

                            if (!pastDirective.isPickup() && pastDirective.getRequest().equals(addedDirective.getRequest())) {
                                continue; // Not a feasible solution
                            }
                        }
                    }

                    // Second, check capacity constraint (TODO: not sure if it is necessary)
                    if (vehicle.isPresent() && addedDirective.isPickup()) {
                        if (partial.passengers + 1 > capacity) {
                            continue; // Not a feasible solution
                        }
                    }

                    // Third, check timing constraints
                    Link originLink = startLink;

                    if (partial.indices.size() > 0) {
                        int precedingIndex = partial.indices.get(partial.indices.size() - 1);
                        StopDirective precedingDirective = directives.get(precedingIndex);
                        originLink = Directive.getLink(precedingDirective);
                    }

                    StopDirective directive = directives.get(index);
                    AlonsoMoraRequest request = associatedRequests.get(index);

                    Link destinationLink = Directive.getLink(directive);
                    double updatedTime = partial.time + travelTimeCalculator.getTravelTime(partial.time, originLink, destinationLink);
                    double updatedCost = partial.cost;

                    if (directive.isPickup()) {
                        double activePickupTime = request.getActivePickupTime();

                        if (parameters.useSoftConstraintsAfterAssignment && request.isAssigned()) {
                            updatedCost += Math.max(0.0, updatedTime - activePickupTime);
                        } else if (updatedTime > activePickupTime) {
                            continue; // Not feasible because pickup will be too late!
                        }

                        updatedTime += pickupDuration;
                    } else {
                        double activeDropoffTime = request.getActiveDropoffTime();

                        if (parameters.useSoftConstraintsAfterAssignment && request.isAssigned()) {
                            updatedCost += Math.max(0.0, updatedTime - activeDropoffTime);
                        } else if (updatedTime > activeDropoffTime) {
                            continue; // Not feasible because dropoff will be too late!
                        }

                        updatedTime += dropoffDuration;
                        updatedCost += Math.max(0.0, updatedTime - request.getDirectDropoffTime());
                    }

                    // Finally, establish new cost
                    if (updatedCost >= minimumCost) {
                        continue; // Early stopping of exploration if cost gets too large
                    }

                    // We have a new feasible partial solution!
                    List<Integer> updatedIndices = new ArrayList<>(partial.indices.size() + 1);
                    updatedIndices.addAll(partial.indices);
                    updatedIndices.add(index);

                    if (updatedIndices.size() < numberOfDirectives) {
                        // Needs to be expanded further
                        queue.add(new PartialSolution(updatedIndices, updatedTime, updatedCost));
                    } else {
                        // We have a full sequence with cost better than the current minimum
                        minimumCost = updatedCost;

                        List<StopDirective> sequence = new ArrayList<>(numberOfDirectives);

                        for (int k = 0; k < numberOfDirectives; k++) {
                            sequence.add(directives.get(updatedIndices.get(k)));
                        }

                        minimumCostSolution = new Result(directives, updatedCost);
                        numberOfSolutions++;
                    }
                }
            }
        }

        return Optional.ofNullable(minimumCostSolution);
    }

    private List<AlonsoMoraRequest> getExistingRequests(AlonsoMoraVehicle vehicle) {
        List<AlonsoMoraRequest> existingRequests = new LinkedList<>();
        IdSet<Request> pickedUpIds = new IdSet<>(Request.class);

        for (Directive directive : vehicle.getDirectives()) {
            if (directive.isModifiable()) { // If not modifiable we ignore it
                if (directive instanceof StopDirective) {
                    StopDirective stopDirective = (StopDirective) directive;

                    if (stopDirective.isPickup()) {
                        pickedUpIds.add(stopDirective.getRequest().getId());
                    } else {
                        if (!pickedUpIds.contains(stopDirective.getRequest().getId())) {
                            existingRequests.add(requests.get(stopDirective.getRequest().getId()));
                        }
                    }
                }
            }
        }

        return new ArrayList<>(existingRequests);
    }

    private class PartialSolution {
        List<Integer> indices;
        double time;
        double cost;
        int passengers;

        PartialSolution(List<Integer> indices, double time, double cost) {
            this.indices = indices;
            this.time = time;
            this.cost = cost;
        }
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraVehicle vehicle, Collection<AlonsoMoraRequest> requests) {
        return optimize(new ArrayList<>(requests), Optional.of(vehicle));
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest) {
        return optimize(Arrays.asList(firstRequest, secondRequest), Optional.empty());
    }
}
