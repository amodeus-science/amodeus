/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.Tensor;

/* package */ class AidoDistanceRecorder {
    private final List<AidoVehicleStatistic> list;

    public AidoDistanceRecorder(int numVehicles, MatsimAmodeusDatabase db) {
        list = IntStream.range(0, numVehicles) //
                .mapToObj(i -> new AidoVehicleStatistic(db)) //
                .collect(Collectors.toList());
    }

    Tensor distance(SimulationObject simulationObject) {
        return simulationObject.vehicles.stream() //
                .map(vehicleContainer -> list.get(vehicleContainer.vehicleIndex).distance(vehicleContainer)) //
                .reduce(Tensor::add) //
                .orElse(StaticHelper.ZEROS.copy());
    }
}
