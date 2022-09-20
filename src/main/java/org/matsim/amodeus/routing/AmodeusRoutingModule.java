package org.matsim.amodeus.routing;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.matsim.amodeus.price_model.PriceModel;
import org.matsim.amodeus.routing.interaction.AmodeusInteractionFinder;
import org.matsim.amodeus.waiting_time.WaitingTime;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.core.router.DefaultRoutingRequest;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.RoutingRequest;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;

public class AmodeusRoutingModule implements RoutingModule {
    static final public String INTERACTION_ACTIVITY_TYPE = "amodeus interaction";

    private final AmodeusRouteFactory routeFactory;
    private final RoutingModule walkRoutingModule;
    private final PopulationFactory populationFactory;
    private final LeastCostPathCalculator router;
    private final PriceModel priceCalculator;
    private final Network network;
    private final TravelTime travelTime;

    private final AmodeusInteractionFinder interactionFinder;
    private final WaitingTime waitingTime;
    private final boolean useAccessEgress;
    private final boolean predictRoute;

    private final String mode;

    public AmodeusRoutingModule(AmodeusRouteFactory routeFactory, AmodeusInteractionFinder interactionFinder,
            WaitingTime waitingTime, PopulationFactory populationFactory,
            RoutingModule walkRoutingModule, boolean useAccessEgress, boolean predictRoute,
            LeastCostPathCalculator router, PriceModel priceCalculator, Network network,
            TravelTime travelTime, String mode) {
        this.routeFactory = routeFactory;
        this.interactionFinder = interactionFinder;
        this.waitingTime = waitingTime;
        this.walkRoutingModule = walkRoutingModule;
        this.populationFactory = populationFactory;
        this.useAccessEgress = useAccessEgress;
        this.predictRoute = predictRoute;
        this.router = router;
        this.priceCalculator = priceCalculator;
        this.mode = mode;
        this.network = network;
        this.travelTime = travelTime;
    }

    @Override
    public List<? extends PlanElement> calcRoute(RoutingRequest routingRequest) {
        Facility fromFacility = routingRequest.getFromFacility();
        Facility toFacility = routingRequest.getToFacility();
        double departureTime = routingRequest.getDepartureTime();
        Person person = routingRequest.getPerson();

        Facility pickupFacility = interactionFinder.findPickupFacility(fromFacility, departureTime);
        Facility dropoffFacility = interactionFinder.findDropoffFacility(toFacility, departureTime);

        if (pickupFacility.getLinkId().equals(dropoffFacility.getLinkId())) {
            // Special case: PassengerEngine will complain that request has same start and
            // end link. In that case we just return walk.
            return walkRoutingModule.calcRoute(routingRequest);
        }

        double pickupEuclideanDistance = CoordUtils.calcEuclideanDistance(fromFacility.getCoord(),
                pickupFacility.getCoord());
        double dropoffEuclideanDistance = CoordUtils.calcEuclideanDistance(dropoffFacility.getCoord(),
                toFacility.getCoord());

        // First, we need to access (if configured)
        double accessDepartureTime = departureTime;
        double requestSendTime = departureTime;

        List<PlanElement> routeElements = new LinkedList<>();

        if (fromFacility != pickupFacility && useAccessEgress && pickupEuclideanDistance > 0.0) {
            RoutingRequest newRequest = DefaultRoutingRequest.of(fromFacility, pickupFacility, accessDepartureTime,
                    person, routingRequest.getAttributes());
            List<? extends PlanElement> pickupElements = walkRoutingModule.calcRoute(newRequest);
            routeElements.addAll(pickupElements);

            Activity pickupActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
                    pickupFacility.getLinkId());
            pickupActivity.setMaximumDuration(0.0);
            routeElements.add(pickupActivity);

            if (pickupElements.size() != 1) {
                throw new IllegalStateException();
            }

            // If we have a non-zero access leg, the request for the AV will be issued after
            // arrival
            requestSendTime += ((Leg) pickupElements.get(0)).getTravelTime().seconds();
        }

        // Now waiting for the vehicle

        // Given the request time, we can calculate the waiting time
        double vehicleWaitingTime = waitingTime.getWaitingTime(pickupFacility, requestSendTime);
        double vehicleDepartureTime = requestSendTime + vehicleWaitingTime;

        // Here we can optionally already route the AV leg to estimate travel time
        // TODO @sebhoerl Maybe we can hide this behind a AVTravelTimePredictor
        // interface or
        // something like that
        double vehicleDistance = Double.NaN;
        double vehicleTravelTime = Double.NaN;
        Optional<Double> price = Optional.empty();

        if (predictRoute) {
            Link pickupLink = network.getLinks().get(pickupFacility.getLinkId());
            Link dropoffLink = network.getLinks().get(dropoffFacility.getLinkId());

            VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(pickupLink, dropoffLink, vehicleDepartureTime,
                    router, travelTime);

            vehicleDistance = VrpPaths.calcDistance(path);
            vehicleTravelTime = path.getTravelTime();

            price = priceCalculator.calculatePrice(requestSendTime, pickupFacility, dropoffFacility, vehicleDistance,
                    vehicleTravelTime);
        }

        double totalTravelTime = vehicleTravelTime + vehicleWaitingTime;

        // Build Route and Leg
        AmodeusRoute route = routeFactory.createRoute(pickupFacility.getLinkId(), dropoffFacility.getLinkId());
        route.setDistance(vehicleDistance);

        if (Double.isFinite(totalTravelTime)) {
            route.setTravelTime(totalTravelTime);
        }

        route.setWaitingTime(vehicleWaitingTime);

        if (price.isPresent()) {
            route.setPrice(price.get());
        }

        Leg leg = populationFactory.createLeg(mode);
        leg.setDepartureTime(requestSendTime);

        if (Double.isFinite(totalTravelTime)) {
            leg.setTravelTime(totalTravelTime);
        }

        leg.setRoute(route);

        routeElements.add(leg);

        // Now we need to egress
        double egressDepartureTime = vehicleDepartureTime; // The last we know, if we don't route

        if (!Double.isNaN(totalTravelTime)) {
            // If we have routed the leg, we know it better
            egressDepartureTime = totalTravelTime;
        }

        if (toFacility != dropoffFacility && useAccessEgress && dropoffEuclideanDistance > 0.0) {
            Activity dropoffActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
                    dropoffFacility.getLinkId());
            dropoffActivity.setMaximumDuration(0.0);
            routeElements.add(dropoffActivity);

            RoutingRequest newRequest = DefaultRoutingRequest.of(dropoffFacility, toFacility, egressDepartureTime,
                    person, routingRequest.getAttributes());
            List<? extends PlanElement> dropoffElements = walkRoutingModule.calcRoute(newRequest);
            routeElements.addAll(dropoffElements);
        }

        return routeElements;
    }
}
