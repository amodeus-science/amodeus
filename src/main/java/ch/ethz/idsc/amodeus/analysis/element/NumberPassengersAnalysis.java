/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.PadRight;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Max;

public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    /** contains the times in s */
    public final Tensor time = Tensors.empty();
    /** contains the number passengers per vehicles for each timestep */
    private final Tensor numberPassengers = Tensors.empty();
    /** contains the distribution of number of passengers per timestep */
    private Tensor passengerDistribution = Tensors.empty();
    /** contains the number of other passengers in the vehicle foreach request */
    private final Tensor sharedOthersPerRequest = Tensors.empty();

    /** Helper members */
    private boolean beforeFirstSimulation = true;
    private Tensor lastNumPassInVs;
    private Scalar maxNumPassengers;
    private final Map<Integer, Integer> requestVehiclePickups = new HashMap<>();
    private final Map<Integer, Set<Integer>> currentPassengers = new HashMap<>();
    private final Map<Integer, Integer> sharedOthersMap = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {

        /** On first Timestep fill the Tensor for the Vehicles as well as the map with the current passengers */
        if (beforeFirstSimulation) {
            lastNumPassInVs = Array.zeros(simulationObject.vehicles.size());
            simulationObject.vehicles.forEach(vc -> currentPassengers.put(vc.vehicleIndex, new HashSet<>()));
            beforeFirstSimulation = false;
        }

        /** build the Number of Requests per Time Step */
        for (RequestContainer requestContainer : simulationObject.requests) {
            // In case a request was picked up in this timestep
            if (requestContainer.requestStatus.contains(RequestStatus.PICKUP)) {
                int vehicleId = requestContainer.associatedVehicle;
                
                lastNumPassInVs.set(lastNumPassInVs.Get(vehicleId).add(RealScalar.ONE), vehicleId);
                // update the map which stores for each pickup which RoboTaxi serves this request
                requestVehiclePickups.put(requestContainer.requestIndex, vehicleId);
                // update the map which stores with how many other requests the trip was shared
                GlobalAssert.that(currentPassengers.get(vehicleId).add(requestContainer.requestIndex));
                updateSharedWith(vehicleId);
            }
            // In case a request was dropped off in this timestep
            if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF)) {
                GlobalAssert.that(requestVehiclePickups.containsKey(requestContainer.requestIndex));
                int vehicleId = requestVehiclePickups.get(requestContainer.requestIndex);
                lastNumPassInVs.set(lastNumPassInVs.Get(vehicleId).subtract(RealScalar.ONE), vehicleId);
                GlobalAssert.that(currentPassengers.get(vehicleId).remove(requestContainer.requestIndex));
            }
        }
        // TEST which can be used to make this calculation easier in the future;
        Tensor numPassengersPerRoboTaxi = Array.zeros(simulationObject.vehicles.size()); // could be done before class
        currentPassengers.forEach((v, rs) -> numPassengersPerRoboTaxi.set(RealScalar.of(rs.size()), v));
        GlobalAssert.that(numPassengersPerRoboTaxi.equals(lastNumPassInVs));
        
        numberPassengers.append(lastNumPassInVs);
        passengerDistribution.append(BinCounts.of(lastNumPassInVs));
        time.append(RealScalar.of(simulationObject.now));
    }

    private void updateSharedWith(int vehicleIndex) {
        Set<Integer> requestsInVehicle = currentPassengers.get(vehicleIndex);
        int numberOtherPassengers = requestsInVehicle.size() - 1;
        for (Integer requestindex : requestsInVehicle) {
            if (sharedOthersMap.containsKey(requestindex)) {
                if (sharedOthersMap.get(requestindex) < numberOtherPassengers) {
                    sharedOthersMap.put(requestindex, numberOtherPassengers);
                }
            } else {
                sharedOthersMap.put(requestindex, numberOtherPassengers);
            }
        }
    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        maxNumPassengers = numberPassengers.flatten(-1).reduce(Max::of).get().Get();
        passengerDistribution = PadRight.zeros(passengerDistribution.length(), maxNumPassengers.number().intValue() + 1).apply(passengerDistribution);

        for (Integer index : sharedOthersMap.keySet()) {
            sharedOthersPerRequest.append(RealScalar.of(sharedOthersMap.get(index)));
        }
    }

    public Tensor getNumberPassengers() {
        return numberPassengers;
    }

    public Scalar getMaxNumPassengers() {
        return maxNumPassengers;
    }

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
