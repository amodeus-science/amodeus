/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.InvertUnlessZero;

public class DistanceElement implements AnalysisElement {
    /** link distances in the network must be in [m] */
    private static final Scalar km2m = RealScalar.of(0.001);

    private int index = 0; // Index for the Simulation Object which is loaded
    private List<VehicleStatistic> list = new ArrayList<>();
    /** vector for instance {10, 20, ...} */
    public final Tensor time = Tensors.empty();
    /** vector for instance { 0.0, 0.2, 0.1, 0.3, ...} */
    public final Tensor occupancyTensor = Tensors.empty();
    public final Set<Integer> requestIndices = new HashSet<>();

    // fields assigned in compile
    Tensor distRatio;
    public Tensor totalDistancesPerVehicle;
    public Tensor distancesOverDay;
    public double totalDistance;
    public double totalDistanceWtCst;
    public double totalDistancePicku;
    public double totalDistanceRebal;
    public double totalDistanceRatio;

    // distRatio;
    public Tensor ratios;

    public DistanceElement(int numVehicles, int size) {
        IntStream.range(0, numVehicles).forEach(i -> list.add(new VehicleStatistic(size)));
    }

    @Override
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));
        simulationObject.requests.stream() //
                .forEach(requestContainer -> requestIndices.add(requestContainer.requestIndex));

        /** Get the Occupancy Ratio per TimeStep */
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        Scalar occupancyRatio = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal()).//
                divide(RealScalar.of(simulationObject.vehicles.size()));
        occupancyTensor.append(occupancyRatio);

        /** register Simulation Object for distance analysis */
        ++index;
        for (VehicleContainer vehicleContainer : simulationObject.vehicles)
            list.get(vehicleContainer.vehicleIndex).register(index, vehicleContainer);
    }

    @Override
    public void consolidate() {
        list.forEach(VehicleStatistic::consolidate);
        
        Tensor distTotal = list.stream().map(vs -> vs.distanceTotal).reduce(Tensor::add).get().multiply(km2m);
        Tensor distWtCst = list.stream().map(vs -> vs.distanceWithCustomer).reduce(Tensor::add).get().multiply(km2m);
        Tensor distPicku = list.stream().map(vs -> vs.distancePickup).reduce(Tensor::add).get().multiply(km2m);
        Tensor distRebal = list.stream().map(vs -> vs.distanceRebalance).reduce(Tensor::add).get().multiply(km2m);
        distRatio = distTotal.map(InvertUnlessZero.FUNCTION).pmul(distWtCst);
        // ---
        distancesOverDay = Transpose.of(Tensors.of(distTotal, distWtCst, distPicku, distRebal, distRatio));

        // total distances driven per vehicle
        totalDistancesPerVehicle = Tensor.of(list.stream().map(vs -> Total.of(vs.distanceTotal))).multiply(km2m);

        // Total Values For one Day
        totalDistance = totalDistancesPerVehicle.stream().reduce(Tensor::add).get().Get().number().doubleValue();
        totalDistanceWtCst = distWtCst.stream().reduce(Tensor::add).get().Get().number().doubleValue();
        totalDistancePicku = distPicku.stream().reduce(Tensor::add).get().Get().number().doubleValue();
        totalDistanceRebal = distRebal.stream().reduce(Tensor::add).get().Get().number().doubleValue();
        totalDistanceRatio = totalDistanceWtCst / totalDistance;
        ratios = Transpose.of(Join.of(Tensors.of(occupancyTensor), Tensors.of(distRatio)));

    }

}
