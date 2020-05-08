/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.PadRight;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Max;

public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    /** contains the times in [s] */
    private final Tensor time = Tensors.empty();
    /** contains the distribution of number of passengers per time step */
    private Tensor passengerDistribution = Tensors.empty();
    /** contains the number of other passengers in the vehicle for each request */
    private final Tensor sharedOtherPassengersPerRequest = Tensors.empty();

    /** Helper members */
    private final Map<Integer, Integer> sharedOthersMap = new HashMap<>();
    private final List<Integer> vehicleIndices;

    public NumberPassengersAnalysis(Set<Integer> vehicleIndices) {
        this.vehicleIndices = new ArrayList<>(vehicleIndices);
    }

    @Override
    public void register(SimulationObject simulationObject) {

        time.append(RealScalar.of(simulationObject.now));
        Map<Integer, List<RequestContainer>> map = simulationObject.requests.stream()//
                .filter(NumPassengerHelper::isrelevantRequstContainer)//
                .collect(Collectors.groupingBy(reqcontainer -> reqcontainer.associatedVehicle));

        /** number Passenger Distribution over day */
        Tensor numberPassengers = Array.zeros(simulationObject.vehicles.size());
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            int numberPassenger = (map.containsKey(vehicleContainer.vehicleIndex)) ? //
                    map.get(vehicleContainer.vehicleIndex).size() : 0;
            numberPassengers.set(RealScalar.of(numberPassenger), vehicleIndices.indexOf(vehicleContainer.vehicleIndex));
        }
        Tensor numPassenger = BinCounts.of(numberPassengers);
        passengerDistribution.append(numPassenger);

        /** AV Request Sharing Rate */
        for (List<RequestContainer> requestsInVehicle : map.values()) {
            int numberOtherPassengers = requestsInVehicle.size() - 1;
            for (RequestContainer reqContainer : requestsInVehicle)
                if (sharedOthersMap.containsKey(reqContainer.requestIndex)) {
                    if (sharedOthersMap.get(reqContainer.requestIndex) < numberOtherPassengers)
                        sharedOthersMap.put(reqContainer.requestIndex, numberOtherPassengers);
                } else
                    sharedOthersMap.put(reqContainer.requestIndex, numberOtherPassengers);
        }
    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        Scalar maxLengthNumberPassengers = Tensor.of(passengerDistribution.stream()//
                .map(t -> RealScalar.of(t.length()))).flatten(-1).reduce(Max::of).get().Get();
        passengerDistribution = //
                PadRight.zeros(passengerDistribution.length(), maxLengthNumberPassengers.number().intValue())//
                        .apply(passengerDistribution);
        // // for (Integer index : sharedOthersMap.keySet())
        // // sharedOtherPassengersPerRequest.append(RealScalar.of(sharedOthersMap.get(index)));
        // for (Integer others : sharedOthersMap.values())
        // sharedOtherPassengersPerRequest.append(RealScalar.of(others));
        sharedOthersMap.values().stream().map(RealScalar::of).forEach(sharedOtherPassengersPerRequest::append);
    }

    public Tensor getPassengerDistribution() {
        return passengerDistribution;
    }

    public Tensor getTime() {
        return time;
    }

    /** @return {@link Tensor} containing the number of trips that shared the {@link RoboTaxi}
     *         with {0,1,2,..} other requests, e.g., {80,40,20,10} which means that there were a total
     *         of 150 requests out of which 80 were driving alone, and 10 shared the {@link RoboTaxi} with
     *         3 other passengers, etc. */
    public Tensor getSharedOthersDistribution() {
        return BinCounts.of(sharedOtherPassengersPerRequest);
    }

    /** @return the maximal number of other customers in the {@link RoboTaxi} for picked up requests.
     *         The order of the requests is not corresponding to the index. */
    public Tensor getSharedOthersPerRequest() {
        return sharedOtherPassengersPerRequest;
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalValues = new HashMap<>();
        // String sharedRateString = "";
        // for (Tensor tensor : getSharedOthersDistribution()) {
        // sharedRateString += String.valueOf(tensor.Get().number().intValue()) + " ";
        // }
        totalValues.put(TtlValIdent.REQUESTSSHAREDNUMBERS, getSharedOthersDistribution().toString());
        return totalValues;
    }

}
