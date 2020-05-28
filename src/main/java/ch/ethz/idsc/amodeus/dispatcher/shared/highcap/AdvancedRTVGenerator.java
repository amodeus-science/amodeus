/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class AdvancedRTVGenerator {
    private static final double MAX_RANGE = 999999.8;
    // ---
    private final Map<RoboTaxi, Set<AVRequest>> feasibleOpenRequestFromLastStepMap = new HashMap<>();
    private final int capacityOfTaxi;
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;

    public AdvancedRTVGenerator(int capacityOfTaxi, double pickupDurationPerStop, double dropoffDurationPerStop) {
        this.capacityOfTaxi = capacityOfTaxi;
        this.pickupDurationPerStop = pickupDurationPerStop;
        this.dropoffDurationPerStop = dropoffDurationPerStop;
    }

    public List<TripWithVehicle> generateRTV(List<RoboTaxi> roboTaxis, Set<AVRequest> newAddedRequests, Set<AVRequest> removedRequests, //
            double now, Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, Set<Set<AVRequest>> rvEdges, //
            TravelTimeComputation ttc, List<TripWithVehicle> lastAssignment, double trafficAllowance) {
        List<TripWithVehicle> grossListOfRTVEdges = new ArrayList<>();
        for (RoboTaxi roboTaxi : roboTaxis) {
            // construct collection of single request to check
            Set<AVRequest> candidateRequests = new HashSet<>();
            // first get feasible request from last step
            if (!feasibleOpenRequestFromLastStepMap.containsKey(roboTaxi)) {
                feasibleOpenRequestFromLastStepMap.put(roboTaxi, new HashSet<>());
                System.out.println("RTV Supplementary map generated for a robotaxi");
            }

            candidateRequests.addAll(feasibleOpenRequestFromLastStepMap.get(roboTaxi));
            // then we add newly added requests and remove request that has been removed from validOpenRequestList
            candidateRequests.addAll(newAddedRequests);
            candidateRequests.removeAll(removedRequests);
            feasibleOpenRequestFromLastStepMap.get(roboTaxi).clear(); // we will write in the new feasible request

            Link taxiCurrentLink = roboTaxi.getDivertableLocation();// get roboTaxi current location

            // change the maxWaitTime of assigned request of this roboTaxi to the original value (there is uncertainty on the road)
            // IMPORTANT!!! deadlines need to be changed back to the old value after we finish dealing with this roboTaxi
            for (TripWithVehicle tripWithVehicle : lastAssignment)
                if (tripWithVehicle.getRoboTaxi() == roboTaxi) {
                    for (AVRequest avRequest : tripWithVehicle.getTrip())
                        if (requestKeyInfoMap.containsKey(avRequest))
                            requestKeyInfoMap.get(avRequest).addTrafficAllowance(trafficAllowance);
                    break;
                }

            // size 1 trips:
            List<Set<AVRequest>> listOfsize1Trip = new ArrayList<>(); // this is useful for generating possible combination for size 2 trip
            for (AVRequest avRequest : candidateRequests) {
                double timeFromTaxiToRequest = ttc.of(taxiCurrentLink, //
                        avRequest.getFromLink(), now, false); // do not store this travel time in Cache.
                double arrivalTime = now + timeFromTaxiToRequest;
                double deadlineForPickUp = requestKeyInfoMap.get(avRequest).getDeadlinePickUp(); // see note about modifiedSubmission Time above
                if (arrivalTime < deadlineForPickUp) { // the request is not too far, we can proceed to route generation/validation
                    Set<AVRequest> additionalRequest = new HashSet<>();
                    additionalRequest.add(avRequest);
                    List<StopInRoute> route = RouteGenerator.of(roboTaxi, additionalRequest, now, //
                            requestKeyInfoMap, capacityOfTaxi, ttc, pickupDurationPerStop, dropoffDurationPerStop);
                    double totalDelayForThisTrip = TotalDelayCalculator.of(route, requestKeyInfoMap, ttc);

                    if (isTripValid(totalDelayForThisTrip)) {
                        // if the route is valid, put this trip into the requestDelayMap
                        // (thisTrip=additionalRequest in size 1 case)
                        // requestDelayMap.put(additionalRequest, totalDelayForThisTrip);
                        TripWithVehicle thisTripWithVehicle = new TripWithVehicle(roboTaxi, totalDelayForThisTrip, additionalRequest, route);
                        if (thisTripWithVehicle.getRoute().isEmpty() && totalDelayForThisTrip != 0)
                            System.err.println("something is wrong");
                        grossListOfRTVEdges.add(thisTripWithVehicle);
                        listOfsize1Trip.add(additionalRequest); // this is useful for generating possible combination for size 2 trip
                        feasibleOpenRequestFromLastStepMap.get(roboTaxi).add(avRequest);
                    }
                }
            }

            // size 2 trips:
            List<Set<AVRequest>> listOfSize2Trips = new ArrayList<>();
            for (int i = 0; i < listOfsize1Trip.size(); i++)
                for (int j = i + 1; j < listOfsize1Trip.size(); j++) {
                    Set<AVRequest> thisTrip = new HashSet<>();
                    thisTrip.add(listOfsize1Trip.get(i).iterator().next());
                    thisTrip.add(listOfsize1Trip.get(j).iterator().next());
                    // check if this trip is in RV graph
                    if (rvEdges.contains(thisTrip)) {
                        List<StopInRoute> route = RouteGenerator.of(roboTaxi, thisTrip, now, //
                                requestKeyInfoMap, capacityOfTaxi, ttc, pickupDurationPerStop, dropoffDurationPerStop);
                        double totalDelayForThisTrip = TotalDelayCalculator.of(route, requestKeyInfoMap, ttc);
                        if (isTripValid(totalDelayForThisTrip)) {
                            TripWithVehicle thisTripWithVehicle = new TripWithVehicle(roboTaxi, totalDelayForThisTrip, thisTrip, route);
                            grossListOfRTVEdges.add(thisTripWithVehicle);
                            listOfSize2Trips.add(thisTrip);
                        }
                    }
                }

            // size 3 to maximum trip length
            List<Set<AVRequest>> listOfTripsFromLastLoop = listOfSize2Trips;
            List<Set<AVRequest>> listOfTripsFromThisLoop = new ArrayList<>();
            int k = 3;
            while (k <= capacityOfTaxi && !listOfTripsFromLastLoop.isEmpty()) {
                // generate all combination of trips with size k
                listOfTripsFromThisLoop.clear();
                for (int i = 0; i < listOfTripsFromLastLoop.size(); i++) {
                    for (int j = i + 1; j < listOfTripsFromLastLoop.size(); j++) {
                        Set<AVRequest> thisTrip = new HashSet<>();
                        thisTrip.addAll(listOfTripsFromLastLoop.get(i));
                        thisTrip.addAll(listOfTripsFromLastLoop.get(j));

                        // check if this trip is size k
                        if (thisTrip.size() == k) {
                            // check if all thisTrip's sub-trip is in the set of trips of size k-1
                            if (EverySubtripIsValid.of(listOfTripsFromLastLoop, thisTrip)) {
                                // if yes, then generate route and validate the route
                                List<StopInRoute> route = RouteGenerator.of(roboTaxi, thisTrip, now, //
                                        requestKeyInfoMap, capacityOfTaxi, //
                                        ttc, pickupDurationPerStop, dropoffDurationPerStop);
                                double totalDelayForThisTrip = //
                                        TotalDelayCalculator.of(route, requestKeyInfoMap, ttc);
                                if (isTripValid(totalDelayForThisTrip)) {
                                    TripWithVehicle thisTripWithVehicle = new TripWithVehicle(roboTaxi, totalDelayForThisTrip, thisTrip, route);
                                    grossListOfRTVEdges.add(thisTripWithVehicle);
                                    listOfTripsFromThisLoop.add(thisTrip);
                                }
                            }
                        }
                    }
                }
                k++;
                listOfTripsFromLastLoop = listOfTripsFromThisLoop;
            }

            // remove some data from cache to release some memory
            if (!roboTaxi.isInStayTask())
                ttc.removeEntry(taxiCurrentLink); // if the taxi is moving (not staying), then this entry will not be useful anymore

            // remove traffic allowance we added before, so that next taxi will get the original value
            for (TripWithVehicle tripWithVehicle : lastAssignment)
                if (tripWithVehicle.getRoboTaxi() == roboTaxi) {
                    for (AVRequest avRequest : tripWithVehicle.getTrip())
                        if (requestKeyInfoMap.keySet().contains(avRequest))
                            requestKeyInfoMap.get(avRequest).removeTrafficAllowance(trafficAllowance);
                    break;
                }
        }
        return grossListOfRTVEdges;
    }

    static boolean isTripValid(double totalDelayForThisTrip) {
        // return totalDelayForThisTrip !=null;
        return totalDelayForThisTrip < MAX_RANGE;
    }
}
