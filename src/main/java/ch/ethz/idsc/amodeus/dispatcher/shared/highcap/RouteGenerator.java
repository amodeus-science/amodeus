/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

/* package */ enum RouteGenerator {
    ;
    /** Route Generator
     * this function generate a route base on current request on board and additional requests to be assigned to the roboTaxi
     * the output is list of stopInRoute (time, link, type(pickup/drop off), avRequest) */
    // input: roboTaxi, additionRequest, allAVRequest(for loading requests on board), now, router
    // output: route (a list of stopInRoute)
    // 20181022 Important update! Stop generating route immediately when the route is not feasible.

    public static List<StopInRoute> of(RoboTaxi roboTaxi, Set<AVRequest> additionalRequest, //
            double now, Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, //
            int capacityOfTaxi, TravelTimeComputation ttc, double pickupDurationPerStop, double dropoffDurationPerStop) {
        // define some variables
        double nowInThisFunction;
        nowInThisFunction = now;
        List<StopInRoute> finalRoute = new ArrayList<>();
        List<NextPossibleStop> nextPossibleStopsList = new ArrayList<>();
        int numberOfPassengerOnboard = (int) roboTaxi.getOnBoardPassengers(); // get initial no. passenger on board.

        for (AVRequest avRequest : OnMenuRequests.getOnBoardRequests(roboTaxi.getUnmodifiableViewOfCourses()))
            nextPossibleStopsList.add(new NextPossibleStop(avRequest, true));// add all on board request to the set

        for (AVRequest avRequest : additionalRequest)
            nextPossibleStopsList.add(new NextPossibleStop(avRequest, false));// add all additional request to the set

        Link currentLink = roboTaxi.getDivertableLocation(); // for looking for first stop, use current taxi location (link)
        // search for next stop (pick the closest stop from current link to be the next stop)
        while (!nextPossibleStopsList.isEmpty()) {
            // find the closest stop from current link
            int indexOfStop = 0;
            double shortestDistance = NetworkUtils.getEuclideanDistance(currentLink.getCoord(), nextPossibleStopsList.get(0).getLink().getCoord());
            // double shortestTime = ttc.of(currentLink, nextPossibleStopsList.get(0).getLink(), nowInThisFunction, true);
            for (int i = 1; i < nextPossibleStopsList.size(); i++) {
                double distance = NetworkUtils.getEuclideanDistance(currentLink.getCoord(), nextPossibleStopsList.get(i).getLink().getCoord());
                // double timeToTravel=ttc.of(currentLink, nextPossibleStopsList.get(i).getLink(), nowInThisFunction, true);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    indexOfStop = i;
                }
                if (currentLink == nextPossibleStopsList.get(0).getLink()) {
                    indexOfStop = 0;
                    break;
                }
                if (currentLink == nextPossibleStopsList.get(i).getLink()) {
                    indexOfStop = i;
                    break;
                }
            }
            NextPossibleStop chosenNextStop = nextPossibleStopsList.get(indexOfStop);
            // add this stop to the output list
            // determine sharedMealType
            SharedMealType stopType = SharedMealType.PICKUP;
            if (chosenNextStop.getOnboardStatus())
                stopType = SharedMealType.DROPOFF; // if request is on board, then it will be drop off task
            // determine time (expected arrival time at that stop)
            double arrivalTime = nowInThisFunction + ttc.of(currentLink, chosenNextStop.getLink(), //
                    nowInThisFunction, true);

            // if arrival time is later than deadline, return null immediately (to save time)
            boolean stopIsValid = //
                    stopIsValid(chosenNextStop, arrivalTime, requestKeyInfoMap, ttc);
            if (!stopIsValid)
                return null;

            // modify number of passenger in vehicle
            if (stopType == SharedMealType.PICKUP) {
                numberOfPassengerOnboard = numberOfPassengerOnboard + 1;
                nowInThisFunction = arrivalTime + pickupDurationPerStop;
            }
            if (stopType == SharedMealType.DROPOFF) {
                numberOfPassengerOnboard = numberOfPassengerOnboard - 1;
                nowInThisFunction = arrivalTime + dropoffDurationPerStop;
            }
            // check if vehicle is overloaded (if overloaded, return null immediately)
            if (numberOfPassengerOnboard > capacityOfTaxi)
                return null; // taxi is overloaded. This route is not feasible

            // If it reached here, this stop is valid. Write it down in the list
            StopInRoute nextStopInRoute = new StopInRoute(arrivalTime, chosenNextStop.getLink(), //
                    stopType, chosenNextStop.getAVRequest());
            finalRoute.add(nextStopInRoute);

            // "move" the taxi to that stop and calculate the next stop (loop)
            currentLink = chosenNextStop.getLink();
            // nowInThisFunction = arrivalTime; // this is done in lines above (depending on stop type, add pickup/drop off time)

            // modify the nextPossibleStopSet
            nextPossibleStopsList.remove(indexOfStop); // need to use list (remove item from set somehow doesn't work well here)
            if (!chosenNextStop.getOnboardStatus()) {
                chosenNextStop.changeOnboardStatus(true);
                nextPossibleStopsList.add(chosenNextStop);
            }
        }
        return finalRoute;
    }

    /** Check if this stop is valid */
    static boolean stopIsValid(NextPossibleStop chosenStop, Double arrivalTime, //
            Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, //
            TravelTimeComputation ttc) {
        RequestKeyInfo requestKeyInfo = requestKeyInfoMap.get(chosenStop.getAVRequest());
        final double deadline = chosenStop.getOnboardStatus() //
                ? requestKeyInfo.getDeadlineDropOff() //
                : requestKeyInfo.getDeadlinePickUp();
        // check if deadline is met
        return arrivalTime <= deadline;
    }
}
