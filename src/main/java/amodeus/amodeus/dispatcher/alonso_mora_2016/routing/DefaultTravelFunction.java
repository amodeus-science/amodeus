package amodeus.amodeus.dispatcher.alonso_mora_2016.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraParameters;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraParameters.RouteSearchType;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.alonso_mora_2016.TravelTimeCalculator;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class DefaultTravelFunction implements AlonsoMoraTravelFunction {
    private final double now;
    private final TravelTimeCalculator travelTimeCalculator;
    private final AlonsoMoraParameters parameters;

    private final double pickupDuration;
    private final double dropoffDuration;

    private final IdMap<Request, AlonsoMoraRequest> requests;

    private final List<Constraint> constraints;

    public DefaultTravelFunction(AlonsoMoraParameters parameters, double now, TravelTimeCalculator travelTimeCalculator, IdMap<Request, AlonsoMoraRequest> requests,
            double pickupDuration, double dropoffDuration, Set<Constraint> constraints) {
        this.now = now;
        this.travelTimeCalculator = travelTimeCalculator;
        this.parameters = parameters;
        this.requests = requests;
        this.pickupDuration = pickupDuration;
        this.dropoffDuration = dropoffDuration;
        this.constraints = new ArrayList<>(constraints);
    }

    private Optional<Result> optimize(List<AlonsoMoraRequest> proposedRequests, Optional<AlonsoMoraVehicle> vehicle) {
        // Prepare vehicle information
        int capacity = Integer.MAX_VALUE;

        List<Link> startLinks = new LinkedList<>();
        double startTime = now;

        List<AlonsoMoraRequest> onboardRequests = Collections.emptyList();
        IdMap<Request, TimeInfo> timeInfo = new IdMap<>(Request.class);

        if (vehicle.isPresent()) {
            InitialState initial = getInitialState(vehicle.get());
            capacity = vehicle.get().getCapacity();
            onboardRequests = initial.requests;
            startLinks.add(initial.link);
            startTime = initial.time;
            timeInfo = getCurrentTimeInfo(vehicle.get(), initial.link, initial.time);
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
        double minimumCost = parameters.unassignedPenalty;
        Result minimumCostSolution = null;
        int numberOfSolutions = 0;

        for (final Link startLink : startLinks) {
            RouteGenerator generator;

            if (parameters.routeSearchType.equals(RouteSearchType.Euclidean)) {
                List<Link> directiveLinks = directives.stream().map(Directive::getLink).collect(Collectors.toList());
                generator = new EuclideanRouteGenerator(parameters.euclideanSearch.failEarly, startLink, directiveLinks, startTime, onboardRequests.size());
            } else if (parameters.routeSearchType.equals(RouteSearchType.Extensive)) {
                generator = new ExtensiveRouteGenerator(startTime, numberOfDirectives, onboardRequests.size(), parameters.extensiveSearch.useDepthFirst);
            } else {
                throw new IllegalStateException();
            }

            while (generator.hasNext() && numberOfSolutions < parameters.routeOptimizationLimit) {
                PartialSolution partial = generator.next();

                // First, we need to check if the added index does not introduce a pickup after a dropoff
                StopDirective addedDirective = directives.get(partial.addedIndex);
                boolean invalidStructure = false;

                if (addedDirective.isPickup()) {
                    for (int pastIndex : partial.indices) {
                        StopDirective pastDirective = directives.get(pastIndex);

                        if (!pastDirective.isPickup()) {
                            if (pastDirective.getRequest().getId().equals(addedDirective.getRequest().getId())) {
                                invalidStructure = true;
                                break; // Not a feasible solution
                            }
                        }
                    }
                }

                if (invalidStructure) {
                    continue;
                }

                // Second, check capacity constraint
                int updatedPassengers = partial.passengers;

                if (addedDirective.isPickup()) {
                    updatedPassengers += 1;

                    if (vehicle.isPresent()) {
                        if (updatedPassengers > capacity) {
                            continue; // Not a feasible solution
                        }
                    }
                } else {
                    updatedPassengers -= 1;
                }

                // Third, check timing constraints
                Link originLink = startLink;

                if (partial.indices.size() > 0) {
                    int precedingIndex = partial.indices.get(partial.indices.size() - 1);
                    StopDirective precedingDirective = directives.get(precedingIndex);
                    originLink = Directive.getLink(precedingDirective);
                }

                AlonsoMoraRequest addedRequest = associatedRequests.get(partial.addedIndex);

                Link destinationLink = Directive.getLink(addedDirective);
                double updatedTime = partial.time + travelTimeCalculator.getTravelTime(partial.time, originLink, destinationLink);
                double updatedCost = partial.cost;

                if (addedDirective.isPickup()) {
                    TimeInfo plannedTiming = timeInfo.get(addedDirective.getRequest().getId());
                    updatedTime += pickupDuration;

                    if (parameters.useSoftConstraintsAfterAssignment && plannedTiming != null) {
                        if (updatedTime > plannedTiming.plannedPickupTime) {
                            // if (updatedTime > addedRequest.getActivePickupTime() + trafficAllowance) {
                            continue;
                        }
                    } else {
                        if (updatedTime > addedRequest.getActivePickupTime()) {
                            continue; // Not feasible because pickup will be too late!
                        }
                    }
                } else {
                    TimeInfo plannedTiming = timeInfo.get(addedDirective.getRequest().getId());

                    if (parameters.useSoftConstraintsAfterAssignment && plannedTiming != null) {
                        if (updatedTime > plannedTiming.plannedDropoffTime) {
                            // if (updatedTime > addedRequest.getActiveDropoffTime() + trafficAllowance) {
                            continue;
                        }
                    } else {
                        if (updatedTime > addedRequest.getActiveDropoffTime()) {
                            continue; // Not feasible because pickup will be too late!
                        }
                    }

                    updatedCost += Math.max(0.0, updatedTime - addedRequest.getDirectDropoffTime());
                    updatedTime += dropoffDuration;
                }

                // Finally, establish new cost
                if (updatedCost >= minimumCost) {
                    continue; // Early stopping of exploration if cost gets too large
                }

                boolean constraintsValid = true;

                for (Constraint constraint : constraints) {
                    if (!constraint.validate(vehicle, directives, partial, updatedTime, updatedPassengers)) {
                        constraintsValid = false;
                        break;
                    }
                }

                if (!constraintsValid) {
                    continue;
                }

                // We have a new feasible partial solution!
                if (partial.indices.size() + 1 < numberOfDirectives) {
                    generator.expand(partial, updatedTime, updatedPassengers, updatedCost);
                } else {
                    // We have a full sequence with cost better than the current minimum
                    minimumCost = updatedCost;

                    List<StopDirective> sequence = new ArrayList<>(numberOfDirectives);

                    for (int k = 0; k < numberOfDirectives - 1; k++) {
                        sequence.add(directives.get(partial.indices.get(k)));
                    }

                    sequence.add(directives.get(partial.addedIndex));

                    minimumCostSolution = new Result(directives, updatedCost);
                    numberOfSolutions++;
                }
            }
        }

        if (minimumCostSolution != null) {
            List<StopDirective> dd = minimumCostSolution.directives;

            if (dd.size() >= 3) {
                if (dd.get(0).isPickup()) {
                    if (!dd.get(1).isPickup()) {
                        if (dd.get(2).isPickup()) {
                            System.err.println("HERE HERE HERE HERE HERE HERE HERE");
                        }
                    }
                }
            }
        }

        return Optional.ofNullable(minimumCostSolution);
    }

    private class InitialState {
        final Link link;
        final double time;
        final List<AlonsoMoraRequest> requests;

        InitialState(Link link, double time, List<AlonsoMoraRequest> requests) {
            this.link = link;
            this.time = time;
            this.requests = requests;
        }
    }

    private InitialState getInitialState(AlonsoMoraVehicle vehicle) {
        List<AlonsoMoraRequest> existingRequests = new LinkedList<>();
        IdSet<Request> pickedUpIds = new IdSet<>(Request.class);

        double startTime = now;
        Link startLink = vehicle.getLocation();

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
            } else {
                startTime += travelTimeCalculator.getTravelTime(startTime, startLink, Directive.getLink(directive));
                startLink = Directive.getLink(directive);

                if (directive instanceof StopDirective) {
                    StopDirective stopDirective = (StopDirective) directive;

                    if (stopDirective.isPickup()) {
                        startTime += pickupDuration;
                    } else {
                        startTime += dropoffDuration;
                    }
                }
            }
        }

        return new InitialState(startLink, startTime, new ArrayList<>(existingRequests));
    }

    public static class PartialSolution {
        final List<Integer> indices;
        final int addedIndex;
        final double time;
        final double cost;
        final int passengers;

        PartialSolution(List<Integer> indices, int addedIndex, double time, double cost, int passengers) {
            this.indices = indices;
            this.addedIndex = addedIndex;
            this.time = time;
            this.cost = cost;
            this.passengers = passengers;
        }
    }

    static public class TimeInfo {
        double plannedPickupTime = Double.NaN;
        double plannedDropoffTime = Double.NaN;
    }

    private IdMap<Request, TimeInfo> getCurrentTimeInfo(AlonsoMoraVehicle vehicle, Link startLink, double startTime) {
        IdMap<Request, TimeInfo> timing = new IdMap<>(Request.class);

        double currentTime = startTime;
        Link currentLink = startLink;

        for (Directive directive : vehicle.getDirectives()) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                currentTime += travelTimeCalculator.getTravelTime(currentTime, currentLink, Directive.getLink(stopDirective));

                timing.computeIfAbsent(stopDirective.getRequest().getId(), id -> new TimeInfo());
                TimeInfo info = timing.get(stopDirective.getRequest().getId());

                if (stopDirective.isPickup()) {
                    currentTime += pickupDuration;
                    info.plannedPickupTime = currentTime;
                } else {
                    info.plannedDropoffTime = currentTime;
                    currentTime += dropoffDuration;
                }
            }
        }

        return timing;
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraVehicle vehicle, Collection<AlonsoMoraRequest> requests) {
        return optimize(new ArrayList<>(requests), Optional.of(vehicle));
    }

    @Override
    public Optional<Result> calculate(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest) {
        return optimize(Arrays.asList(firstRequest, secondRequest), Optional.empty());
    }

    public static interface Constraint {
        boolean validate(Optional<AlonsoMoraVehicle> vehicle, List<StopDirective> directives, PartialSolution partial, double updatedTime, int updatedPassengers);
    }
}
