/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Quantile;

/* package */ enum StaticHelper {
    ;

    public static Tensor getNumStatus(SimulationObject simOjb) {
        Tensor numStatus = Array.zeros(RoboTaxiStatus.values().length);
        Map<RoboTaxiStatus, List<VehicleContainer>> map = simOjb.vehicles.stream() //
                .collect(Collectors.groupingBy(vehicleContainer -> vehicleContainer.roboTaxiStatus));
        for (Entry<RoboTaxiStatus, List<VehicleContainer>> entry : map.entrySet())
            numStatus.set(RealScalar.of(entry.getValue().size()), entry.getKey().ordinal());
        return numStatus;
    }

    public static Tensor quantiles(Tensor submission, Tensor param) {
        if (Tensors.isEmpty(submission))
            return Array.zeros(param.length());
        return Quantile.of(submission, param);
    }

    public static Scalar means(Tensor submission) {
        return Tensors.isEmpty(submission) ? RealScalar.ZERO : (Scalar) Mean.of(submission);
    }

    /** for small scenarios, a filter is necessary to obain smooth waiting times plots */
    public static final int FILTERSIZE = 50;
    public static final boolean FILTER_ON = true;

    public static String[] descriptions() {
        return EnumSet.allOf(RoboTaxiStatus.class).stream() //
                .map(RoboTaxiStatus::description) //
                .toArray(String[]::new);
    }

    public static Tensor getNumPassenger(SimulationObject simulationObject) {
        Map<Integer, List<RequestContainer>> map = simulationObject.requests.stream().filter(rc -> rc.requestStatus.contains(RequestStatus.PICKUP) || //
                rc.requestStatus.contains(RequestStatus.DRIVING) //
                || rc.requestStatus.contains(RequestStatus.DROPOFF)) //
                .collect(Collectors.groupingBy(reqcontainer -> reqcontainer.associatedVehicle));
        Tensor numberPassengers = Array.zeros(simulationObject.vehicles.size());
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            int numberPassenger = (map.containsKey(vehicleContainer.vehicleIndex))? map.get(vehicleContainer.vehicleIndex).size() : 0;  
            numberPassengers.set(RealScalar.of(numberPassenger), vehicleContainer.vehicleIndex);
        }
        return BinCounts.of(numberPassengers);
    }
}
