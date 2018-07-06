/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifiersAmodeus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.simobj.SimulationObject;
import ch.ethz.idsc.amodeus.net.simobj.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.InvertUnlessZero;

public class DistanceElement implements AnalysisElement, TotalValueAppender {
    /** link distances in the network must be in [m] */
    private static final Scalar km2m = RealScalar.of(0.001);

    private int simObjIndex = 0; // Index for the Simulation Object which is loaded
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
    public double avgTripDistance;
    public double avgOccupancy;

    // distRatio;
    public Tensor ratios;

    // total Values for TotalValuesFile
    private final Map<TotalValueIdentifier, String> totalValues = new HashMap<>();

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
        avgOccupancy = Mean.of(occupancyTensor).Get().number().doubleValue();

        /** register Simulation Object for distance analysis */
        for (VehicleContainer vehicleContainer : simulationObject.vehicles)
            list.get(vehicleContainer.vehicleIndex).register(simObjIndex, vehicleContainer);

        ++simObjIndex;
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
        avgTripDistance = totalDistanceWtCst / requestIndices.size();
        ratios = Transpose.of(Join.of(Tensors.of(occupancyTensor), Tensors.of(distRatio)));

    }

    /** @return newest distances available {distTotal,distWtCst} */
    public Tensor getNewestDistances() {
        return list.stream() //
                .map(VehicleStatistic::getLatestRecordings) //
                .map(tensor -> tensor.extract(0, 2)) //
                .reduce(Tensor::add) //
                .orElse(Array.zeros(2));
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        totalValues.put(TotalValueIdentifiersAmodeus.TOTALROBOTAXIDISTANCE, String.valueOf(totalDistance));
        totalValues.put(TotalValueIdentifiersAmodeus.TOTALROBOTAXIDISTANCEPICKU, String.valueOf(totalDistancePicku));
        totalValues.put(TotalValueIdentifiersAmodeus.TOTALROBOTAXIDISTANCEWTCST, String.valueOf(totalDistanceWtCst));
        totalValues.put(TotalValueIdentifiersAmodeus.TOTALROBOTAXIDISTANCEREB, String.valueOf(totalDistanceRebal));
        totalValues.put(TotalValueIdentifiersAmodeus.DISTANCERATIO, String.valueOf(totalDistanceRatio));
        totalValues.put(TotalValueIdentifiersAmodeus.OCCUPANCYRATIO, String.valueOf(avgOccupancy));
        totalValues.put(TotalValueIdentifiersAmodeus.AVGTRIPDISTANCE, String.valueOf(avgTripDistance));
        return totalValues;
    }

}
