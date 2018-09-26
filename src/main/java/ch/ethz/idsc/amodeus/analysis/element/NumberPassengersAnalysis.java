/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.Map;

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
import ch.ethz.idsc.tensor.io.Pretty;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Max;

public class NumberPassengersAnalysis implements AnalysisElement, TotalValueAppender {

    private final Tensor numberPassengers = Tensors.empty();
    private Tensor passengerDistribution = Tensors.empty();
    private final Tensor numPassAgg = Tensors.empty();
    public final Tensor time = Tensors.empty();
    private boolean numberPassengersToFill = true;
    private Tensor lastNumPassInVs;
    private Scalar maxNumPassengers;

    private final Map<Integer, Integer> requestVehiclePickups = new HashMap<>();
    
    private int dropoffs = 0;
    private int pickups = 0;
    public NumberPassengersAnalysis() {

    }

    @Override
    public void register(SimulationObject simulationObject) {
        /** On first Timestep fill the number passengers Map */

        if (numberPassengersToFill) {
            lastNumPassInVs = Array.zeros(simulationObject.vehicles.size());
            // for (VehicleContainer vc : simulationObject.vehicles) {
            // numberPassengers.put(vc.vehicleIndex, Tensors.empty());
            // }
            numberPassengersToFill = false;
        }
        /** build the Number of Requests per Time Step */
        for (RequestContainer requestContainer : simulationObject.requests) {

            if (requestContainer.requestStatus.contains(RequestStatus.PICKUP)) {
                Scalar lastNumberPassenger = lastNumPassInVs.Get(requestContainer.associatedVehicle);
                lastNumPassInVs.set(lastNumberPassenger.add(RealScalar.ONE), requestContainer.associatedVehicle);
                requestVehiclePickups.put(requestContainer.requestIndex, requestContainer.associatedVehicle);
                pickups++;
            }
            if (requestContainer.requestStatus.contains(RequestStatus.DROPOFF)) {
                GlobalAssert.that(requestVehiclePickups.containsKey(requestContainer.requestIndex));
                // TODO It should be possible that the dropoff vehicle is stored in the Request Container
                int vehicleIndex = requestVehiclePickups.get(requestContainer.requestIndex);
                Scalar lastNumberPassengerDropOff = lastNumPassInVs.Get(vehicleIndex);
                lastNumPassInVs.set(lastNumberPassengerDropOff.subtract(RealScalar.ONE), vehicleIndex);
                dropoffs++;
            }
        }
        numberPassengers.append(lastNumPassInVs);
        Tensor timeStepHistogram = BinCounts.of(lastNumPassInVs);
        passengerDistribution.append(timeStepHistogram);
        time.append(RealScalar.of(simulationObject.now));
    }

    @Override
    public void consolidate() {
        /** calculate standard dropoff time. */
        maxNumPassengers = numberPassengers.flatten(-1).reduce(Max::of).get().Get();
        System.out.println(Pretty.of(passengerDistribution));
        try {
            //sleep 5 seconds
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }       
        System.out.println("Total number Pickups: " + pickups);
        System.out.println("Total number Dropoffs: " + dropoffs); 

        passengerDistribution = PadRight.zeros(passengerDistribution.length(), maxNumPassengers.number().intValue()+1).apply(passengerDistribution);
        System.out.println(Pretty.of(passengerDistribution));

        
    }

    public Tensor getNumberPassengers() {
        return numberPassengers;
    }

    public Tensor getTime() {
        return time;
    }

    public Tensor getNumPassAgg() {
        return numPassAgg;
    }

    public Scalar getMaxNumPassengers() {
        return maxNumPassengers;
    }
    
    public Tensor getPassengerDistribution() {
        return passengerDistribution;
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalValues = new HashMap<>();
        // totalValues.put(TtlValIdent.AVERAGEJOURNEYTIMEROBOTAXI, String.valueOf(Mean.of(getTotalJourneyTimes()).Get().number().doubleValue()));

        return totalValues;
    }
}
