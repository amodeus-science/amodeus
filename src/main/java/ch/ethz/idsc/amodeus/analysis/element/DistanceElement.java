/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.qty.UnitConvert;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

public class DistanceElement implements AnalysisElement, TotalValueAppender {
    // TODO @joel maybe make customizable or assign task to somebody else
    public static final Unit TARGET_UNIT = Unit.of("km");
    // ---

    private int simObjIndex = 0; // Index for the Simulation Object which is loaded
    private List<VehicleTraceAnalyzer> traceAnalyzers = new ArrayList<>();
    /** vector for instance {10, 20, ...} */
    public final Tensor time = Tensors.empty();

    private final List<Long> times = new ArrayList<>();

    /** vector for instance { 0.0, 0.2, 0.1, 0.3, ...} */
    public final Tensor occupancyTensor = Tensors.empty();
    public final Set<Integer> requestIndices = new HashSet<>();

    /** fields assigned in compile */
    // done
    public Tensor totalDistancesPerVehicle = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistance = RealScalar.of(-1); // initialized to avoid errors in later steps

    // open
    // TODO document what is in here...
    public Tensor distancesOverDay = Tensors.empty(); // initialized to avoid errors in later steps

    public Scalar totalDistanceWtCst = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistancePicku = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistanceRebal = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistanceRatio = RealScalar.of(-1); // initialized to avoid errors in later steps

    private Scalar avgTripDistance = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar avgOccupancy = RealScalar.of(-1); // initialized to avoid errors in later steps

    /** distRatio */
    public Tensor ratios;

    /** variable to check for other classes if the consolidation already happened */
    public boolean consolidated = false;

    public DistanceElement(int numVehicles, int size, MatsimAmodeusDatabase db) {
        IntStream.range(0, numVehicles).forEach(i -> traceAnalyzers.add(new VehicleTraceAnalyzer(size, db)));
    }

    @Override
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));
        times.add(simulationObject.now);
        simulationObject.requests.forEach(requestContainer -> requestIndices.add(requestContainer.requestIndex));

        /** Get the Occupancy Ratio per TimeStep */
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        Scalar occupancyRatio = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal()).//
                divide(RealScalar.of(simulationObject.vehicles.size()));
        occupancyTensor.append(occupancyRatio);
        avgOccupancy = Mean.of(occupancyTensor).Get();

        /** register Simulation Object for distance analysis */
        for (VehicleContainer vehicleContainer : simulationObject.vehicles) {
            // FIXME solve the very very very bad speed sproblem here! TODO big massive FIXME FIXME
            traceAnalyzers.get(vehicleContainer.vehicleIndex)//
                    .register(simObjIndex, vehicleContainer, simulationObject.now);
        }

        ++simObjIndex;
    }

    @Override
    public void consolidate() {
        /** preparing steps */
        ScalarUnaryOperator any2target = UnitConvert.SI().to(TARGET_UNIT);
        traceAnalyzers.forEach(VehicleTraceAnalyzer::consolidate);

        /** calculation of values */
        // total distances driven per vehicle
        totalDistancesPerVehicle = Tensor.of(traceAnalyzers.stream().map(vs -> vs.vehicleTotalDistance)).map(any2target);
        // total distance
        totalDistance = Total.ofVector(totalDistancesPerVehicle);//
        // distance per time of day
        distancesOverDay.append(Tensors.vector(0, 0, 0, 0));
        for (int i = 1; i < times.size() - 1; ++i) {
            Long lTime = times.get(i - 1);
            Long uTime = times.get(i);
            Tensor stepDistance = Tensors.vector(0,0,0,0);
            for (VehicleTraceAnalyzer tA : traceAnalyzers) {
                stepDistance = stepDistance.add(tA.distanceAtStep(lTime, uTime));
            }
            distancesOverDay.append(stepDistance);
        }
        distancesOverDay.append(Tensors.vector(0, 0, 0, 0));
    }

    /** @return An unmodifiable List of all the Vehicle Statistics for all Vehicles
     *         in the fleet. */
    public List<VehicleTraceAnalyzer> getVehicleStatistics() {
        return Collections.unmodifiableList(traceAnalyzers);
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCE, String.valueOf(totalDistance));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEPICKU, String.valueOf(totalDistancePicku));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEWTCST, String.valueOf(totalDistanceWtCst));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEREB, String.valueOf(totalDistanceRebal));
        map.put(TtlValIdent.DISTANCERATIO, String.valueOf(totalDistanceRatio));
        map.put(TtlValIdent.OCCUPANCYRATIO, String.valueOf(avgOccupancy));
        map.put(TtlValIdent.AVGTRIPDISTANCE, String.valueOf(avgTripDistance));
        return map;
    }

}