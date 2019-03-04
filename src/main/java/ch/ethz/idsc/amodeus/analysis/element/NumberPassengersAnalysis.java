/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
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
import ch.ethz.idsc.tensor.red.Total;

// TODO thoroughly refactor this class. 
public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    /** contains the times in [s] */
    private final Tensor time = Tensors.empty();
    /** contains the distribution of number of passengers per time step */
    private Tensor passengerDistribution = Tensors.empty();
    /** contains the number of other passengers in the vehicle for each request */
    private final Tensor sharedOthersPerRequest = Tensors.empty();

    /** Helper members */
    private final Map<Integer, Integer> sharedOthersMap = new HashMap<>();

    @Override
    public void register(SimulationObject simulationObject) {

        time.append(RealScalar.of(simulationObject.now));

        Map<Integer, List<RequestContainer>> map = simulationObject.requests.stream()//
                .filter(rc -> (rc.requestStatus.contains(RequestStatus.PICKUP) //
                        || rc.requestStatus.contains(RequestStatus.DRIVING)) && //
                        !rc.requestStatus.contains(RequestStatus.DROPOFF)) //
                .collect(Collectors.groupingBy(reqcontainer -> reqcontainer.associatedVehicle));

        /** number Passenger Distribution over day */
        Tensor numberPassengers = Array.zeros(simulationObject.vehicles.size());
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            int numberPassenger = (map.containsKey(vehicleContainer.vehicleIndex)) ? map.get(vehicleContainer.vehicleIndex).size() : 0;
            numberPassengers.set(RealScalar.of(numberPassenger), vehicleContainer.vehicleIndex);
        }
        Tensor numPassenger = BinCounts.of(numberPassengers);
        passengerDistribution.append(numPassenger);
        // Control: OLD Calculation
        Scalar numWithCustomer = StaticHelper.getNumStatus(simulationObject)//
                .Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal());
        if (!(Total.of(numPassenger.extract(1, numPassenger.length())).equals(numWithCustomer))) {
            System.err.println("numWithCustomer from passenger: " + Total.of(numPassenger.extract(1, numPassenger.length())));
            System.err.println("numWithcustomer: " + numWithCustomer);
            System.err.println("numStatus: " + StaticHelper.getNumStatus(simulationObject));
            System.err.println("number of passengers calculated from distribution and robotaxi");
            System.err.println("status is not coherent, check file NumberPassengersAnalysis.java");
        }

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

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        Scalar maxLengthNumberPassengers = Tensor.of(passengerDistribution.stream().map(t -> RealScalar.of(t.length()))).flatten(-1).reduce(Max::of).get().Get();
        passengerDistribution = PadRight.zeros(passengerDistribution.length(), maxLengthNumberPassengers.number().intValue()).apply(passengerDistribution);

        for (Integer index : sharedOthersMap.keySet()) {
            sharedOthersPerRequest.append(RealScalar.of(sharedOthersMap.get(index)));
        }
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

    /** @return the maximal number of other customers in the {@link RoboTaxi} for picked up requests.
     *         The order of the requests is not corresponding to the index. */
    public Tensor getSharedOthersPerRequest() {
        return sharedOthersPerRequest;
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalValues = new HashMap<>();
        String sharedRateString = "";
        for (Tensor tensor : getSharedOthersDistribution()) {
            sharedRateString += String.valueOf(tensor.Get().number().intValue()) + " ";
        }
        totalValues.put(TtlValIdent.REQUESTSSHAREDNUMBERS, sharedRateString);
        return totalValues;
    }
}
