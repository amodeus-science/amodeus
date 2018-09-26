/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

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
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.PadRight;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Max;

public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    public final Tensor time = Tensors.empty();

    private final Tensor numberPassengers = Tensors.empty();
    private Tensor passengerDistribution = Tensors.empty();
    private boolean beforeFirstSimulationObject = true;
    private Tensor lastNumPassInVs;
    private Scalar maxNumPassengers;

    private final Map<Integer, Integer> requestVehiclePickups = new HashMap<>();
    private final Map<Integer, Set<Integer>> currentPassengers = new HashMap<>();
    private Tensor sharedWithNOthers;
    private Tensor maxOtherSharedDis;

    @Override
    public void register(SimulationObject simulationObject) {
        /** On first Timestep fill the number passengers Map */

        if (beforeFirstSimulationObject) {
            lastNumPassInVs = Array.zeros(simulationObject.vehicles.size());
            sharedWithNOthers = Array.zeros(simulationObject.requests.size());
            simulationObject.vehicles.forEach(vc -> currentPassengers.put(vc.vehicleIndex, new HashSet<>()));
            beforeFirstSimulationObject = false;
        }
        /** build the Number of Requests per Time Step */
        for (RequestContainer requestContainer : simulationObject.requests) {

            if (requestContainer.requestStatus.contains(RequestStatus.PICKUP)) {
                Scalar lastNumberPassenger = lastNumPassInVs.Get(requestContainer.associatedVehicle);
                lastNumPassInVs.set(lastNumberPassenger.add(RealScalar.ONE), requestContainer.associatedVehicle);
                requestVehiclePickups.put(requestContainer.requestIndex, requestContainer.associatedVehicle);

                boolean result = currentPassengers.get(requestContainer.associatedVehicle).add(requestContainer.requestIndex);
                GlobalAssert.that(result);
                updateSharedWith(requestContainer.associatedVehicle);
            }
            if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF)) {
                GlobalAssert.that(requestVehiclePickups.containsKey(requestContainer.requestIndex));
                // TODO It should be possible that the dropoff vehicle is stored in the Request Container
                int vehicleIndex = requestVehiclePickups.get(requestContainer.requestIndex);
                Scalar lastNumberPassengerDropOff = lastNumPassInVs.Get(vehicleIndex);
                lastNumPassInVs.set(lastNumberPassengerDropOff.subtract(RealScalar.ONE), vehicleIndex);

                boolean result = currentPassengers.get(requestContainer.associatedVehicle).remove(requestContainer.requestIndex);
                GlobalAssert.that(result);
            }
        }
        numberPassengers.append(lastNumPassInVs);
        Tensor timeStepHistogram = BinCounts.of(lastNumPassInVs);
        passengerDistribution.append(timeStepHistogram);
        time.append(RealScalar.of(simulationObject.now));
    }

    private void updateSharedWith(int vehicleIndex) {
        Set<Integer> requestsInVehicle = currentPassengers.get(vehicleIndex);
        Scalar numberOtherPassengers = RealScalar.of(requestsInVehicle.size()).subtract(RealScalar.ONE);
        for (Integer requestindex : requestsInVehicle) {
            if (Scalars.lessThan(sharedWithNOthers.Get(requestindex), numberOtherPassengers)) {
                sharedWithNOthers.set(numberOtherPassengers, requestindex);
            }
        }

    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        maxNumPassengers = numberPassengers.flatten(-1).reduce(Max::of).get().Get();
        passengerDistribution = PadRight.zeros(passengerDistribution.length(), maxNumPassengers.number().intValue() + 1).apply(passengerDistribution);

        /** max Shared number per request aggregate */
        maxOtherSharedDis = BinCounts.of(sharedWithNOthers);
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

    public Tensor getMaxOtherSharedDis() {
        return maxOtherSharedDis;
    }

    public Tensor getSharedWithNOthers() {
        return sharedWithNOthers;
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalValues = new HashMap<>();
        // totalValues.put(TtlValIdent.AVERAGEJOURNEYTIMEROBOTAXI, String.valueOf(Mean.of(getTotalJourneyTimes()).Get().number().doubleValue()));

        return totalValues;
    }
}
