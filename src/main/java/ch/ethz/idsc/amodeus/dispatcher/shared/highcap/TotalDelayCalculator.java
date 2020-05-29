/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum TotalDelayCalculator {

    ;
    /** the function (TotalDelayCalculator.of) intakes the route (a list of stopInRoute)
     * and returns totalDelay (double) which represents the total delay of the route
     * (trip). If 999999.9 is returned, it indicates the route (trip) is invalid.
     * Update 20181022 this function has been greatly changed in accordance with route generator for shorter running time.
     * 
     * /* package */
    public static double of(List<StopInRoute> route, //
            Map<PassengerRequest, RequestKeyInfo> requestKeyInfoMap, //
            TravelTimeComputation ttc) {

        if (Objects.isNull(route))
            return 999999.9; // if (greedy) route is not valid, route generator will return null.

        // if it reach here, the greedy route is feasible, now we need to calculate total delay
        double totalDelay = 0.0;
        // List<StopInRoute> thisRoute = new ArrayList<>(route); //in order not to modify the original route, we copy it to thisRoute.

        for (StopInRoute stopInRoute : route)
            if (stopInRoute.getStopType() == SharedMealType.DROPOFF) {
                PassengerRequest requestOfTheStop = stopInRoute.getavRequest();
                double modifiedSubmissionTime = requestKeyInfoMap.get(requestOfTheStop).getModifiableSubmissionTime();
                double timeToTravel = ttc.of(requestOfTheStop.getFromLink(), //
                        requestOfTheStop.getToLink(), modifiedSubmissionTime, true);
                double bestArrivalTime = modifiedSubmissionTime + timeToTravel;
                double delayOfThisStop = stopInRoute.getTime() - bestArrivalTime;
                totalDelay = totalDelay + delayOfThisStop;
            }
        if (totalDelay < 0)
            System.err.println("something may be wrong with the total Delay");
        return totalDelay;
    }
}

// TODO @ChengQi
// Carl if you make the Stop in Route an extension of Shared Course you can use the following functionalities:
// Or use the following helper line
// List<SharedCourse> helper = route.stream().map(sir->sir.name()).collect(Collectors.toList());
// SharedCourseListUtils.checkMenuDoesNotPlanToPickUpMoreCustomersThanCapacity(route, thisRoboTaxi.getCapacity());