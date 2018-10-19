/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.PadRight;
import ch.ethz.idsc.tensor.io.Pretty;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Total;

public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    /** contains the times in s */
    public final Tensor time = Tensors.empty();
    /** contains the number passengers per vehicles for each timestep */
//    private final Tensor numberPassengers = Tensors.empty();
    /** contains the distribution of number of passengers per timestep */
    private Tensor passengerDistribution = Tensors.empty();
    /** contains the number of other passengers in the vehicle foreach request */
    private final Tensor sharedOthersPerRequest = Tensors.empty();

    /** Helper members */
//    private boolean beforeFirstSimulation = true;
    private Scalar maxNumPassengers;
//    private final Map<Integer, Integer> requestVehiclePickups = new HashMap<>();
    private final Map<Integer, Integer> sharedOthersMap = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {

//        /** On first Timestep fill the Tensor for the Vehicles as well as the map with the current passengers */
//        if (beforeFirstSimulation) {
//            simulationObject.vehicles.forEach(vc -> currentPassengers.put(vc.vehicleIndex, new HashSet<>()));
//            beforeFirstSimulation = false;
//        }
//
//        /** build the Number of Requests per Time Step */
//        for (RequestContainer requestContainer : simulationObject.requests) {
//            // In case a request was picked up in this timestep
//            if (requestContainer.requestStatus.contains(RequestStatus.PICKUP)) {
//                int vehicleId = requestContainer.associatedVehicle;
//
//                // update the map which stores for each pickup which RoboTaxi serves this request
//                requestVehiclePickups.put(requestContainer.requestIndex, vehicleId);
//                // update the map which stores with how many other requests the trip was shared
//                GlobalAssert.that(currentPassengers.get(vehicleId).add(requestContainer.requestIndex));
//            }
//            // In case a request was dropped off in this timestep
//            if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF)) {
//                GlobalAssert.that(requestVehiclePickups.containsKey(requestContainer.requestIndex));
//                int vehicleId = requestVehiclePickups.get(requestContainer.requestIndex);
//                GlobalAssert.that(currentPassengers.get(vehicleId).remove(requestContainer.requestIndex));
//            }
//        }
//
//        // Update the sharing graph for all the vehicles
//        updateSharedWith();

        time.append(RealScalar.of(simulationObject.now));
        
        

        Map<Integer, List<RequestContainer>> map = simulationObject.requests.stream().filter(rc -> rc.requestStatus.contains(RequestStatus.PICKUP) || //
                rc.requestStatus.contains(RequestStatus.DRIVING) //
                || rc.requestStatus.contains(RequestStatus.DROPOFF)) //
                .collect(Collectors.groupingBy(reqcontainer -> reqcontainer.associatedVehicle));
        
        /** number Passenger Distribution over day*/
        Tensor numberPassengers = Array.zeros(simulationObject.vehicles.size());
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            int numberPassenger = (map.containsKey(vehicleContainer.vehicleIndex))? map.get(vehicleContainer.vehicleIndex).size() : 0;  
            numberPassengers.set(RealScalar.of(numberPassenger), vehicleContainer.vehicleIndex);
        }
        Tensor numPassenger = BinCounts.of(numberPassengers);
        passengerDistribution.append(numPassenger);
        // Controll:
        // OLD Calculation
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        Scalar numWithCustomer = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal());
        GlobalAssert.that(Total.of(numPassenger.extract(1, numPassenger.length())).equals(numWithCustomer));
   
        /** AV Request Sharing Rate */
        for (List<RequestContainer> requestsInVehicle : map.values()) {
            int numberOtherPassengers = requestsInVehicle.size() - 1;
            for (RequestContainer reqContainer : requestsInVehicle) {
                if (sharedOthersMap.containsKey(reqContainer.requestIndex)) {
                    if (sharedOthersMap.get(reqContainer.requestIndex) < numberOtherPassengers) {
                        sharedOthersMap.put(reqContainer.requestIndex, numberOtherPassengers);
                    }
                } else {
                    sharedOthersMap.put(reqContainer.requestIndex, numberOtherPassengers);
                }
            }
        }
    }

//    private void updateSharedWith() {
//        for (Set<Integer> requestsInVehicle : currentPassengers.values()) {
//            int numberOtherPassengers = requestsInVehicle.size() - 1;
//            for (Integer requestindex : requestsInVehicle) {
//                if (sharedOthersMap.containsKey(requestindex)) {
//                    if (sharedOthersMap.get(requestindex) < numberOtherPassengers) {
//                        sharedOthersMap.put(requestindex, numberOtherPassengers);
//                    }
//                } else {
//                    sharedOthersMap.put(requestindex, numberOtherPassengers);
//                }
//            }
//        }
//    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        maxNumPassengers = Tensor.of(passengerDistribution.stream().map(t-> RealScalar.of(t.length()))).flatten(-1).reduce(Max::of).get().Get();
        passengerDistribution = PadRight.zeros(passengerDistribution.length(), maxNumPassengers.number().intValue() + 1).apply(passengerDistribution);

        for (Integer index : sharedOthersMap.keySet()) {
            sharedOthersPerRequest.append(RealScalar.of(sharedOthersMap.get(index)));
        }
    }

//    public Tensor getNumberPassengers() {
//        return numberPassengers;
//    }

//    public Scalar getMaxNumPassengers() {
//        return maxNumPassengers;
//    }

    public Tensor getPassengerDistribution() {
        return passengerDistribution;
    }

    public Tensor getTime() {
        return time;
    }

    public Tensor getSharedOthersDistribution() {
        return BinCounts.of(sharedOthersPerRequest);
    }

    /** @return the maximal number of other customer in the Robo Taxi for picked up requests. The order of the Requests is not corresponding to the index. */
    public Tensor getSharedOthersPerRequest() {
        return sharedOthersPerRequest;
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        // in case a map with a single entry is required, use Collections.singletonMap(...)
        // totalValues.put(TtlValIdent.AVERAGEJOURNEYTIMEROBOTAXI, String.valueOf(Mean.of(getTotalJourneyTimes()).Get().number().doubleValue()));
        return Collections.emptyMap();
    }
}
