/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisElement;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public class AidoDistanceRecorder implements AnalysisElement {

    private int simObjIndex = 0; // Index for the Simulation Object which is loaded
    private List<AidoVehicleStatistic> list = new ArrayList<>();
    private Tensor distance;

    public AidoDistanceRecorder(int numVehicles) {
        IntStream.range(0, numVehicles).forEach(i -> list.add(new AidoVehicleStatistic()));
    }

    @Override
    public void register(SimulationObject simulationObject) {
        /** register Simulation Object for distance analysis */
        distance = Tensors.of(Quantity.of(0, SI.METER), Quantity.of(0, SI.METER));
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            distance = distance.add(list.get(vehicleContainer.vehicleIndex).register(simObjIndex, vehicleContainer));
        }
        ++simObjIndex;
    }

    public Tensor getDistance() {
        return distance;
    }

}
