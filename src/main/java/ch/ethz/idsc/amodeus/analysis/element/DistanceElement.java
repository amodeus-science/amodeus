/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.CompatibleUnitQ;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.qty.UnitConvert;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

public class DistanceElement implements AnalysisElement, TotalValueAppender {
    private static Unit TARGET_UNIT = Unit.of("km");

    // ---
    private final Map<Integer, VehicleTraceAnalyzer> traceAnalyzers;

    /** vector for instance {10, 20, ...} */
    public final Tensor time = Tensors.empty();
    private final List<Long> times = new ArrayList<>();

    /** fields assigned in compile */
    public Tensor totalDistancesPerVehicle = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistance = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistanceWtCst = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistancePicku = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistanceRebal = RealScalar.of(-1); // initialized to avoid errors in later steps
    public Scalar totalDistanceRatio = RealScalar.of(-1); // initialized to avoid errors in later steps
    /** this contains the distances traveled in each time step with the format of a row being:
     * {total distance, with customer,pickup,rebalance} */
    public Tensor distancesOverDay = Tensors.empty(); // initialized to avoid errors in later steps
    public Tensor distanceRatioOverDay = Tensors.empty();
    public Scalar avgTripDistance = RealScalar.of(-1);

    private final RequestRobotaxiInformationElement requestElement;

    public DistanceElement(Set<Integer> vehicleIndices, MatsimAmodeusDatabase db, //
            RequestRobotaxiInformationElement requestElement) {
        traceAnalyzers = vehicleIndices.stream().collect(Collectors.toMap(Function.identity(), i -> new VehicleTraceAnalyzer(db)));
        this.requestElement = requestElement;
    }

    @Override
    public void register(SimulationObject simulationObject) {
        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));
        times.add(simulationObject.now);

        /** register Simulation Object for distance analysis */
        for (VehicleContainer vehicleContainer : simulationObject.vehicles)
            traceAnalyzers.get(vehicleContainer.vehicleIndex) //
                    .register(vehicleContainer, simulationObject.now);
    }

    @Override // from AnalysisElement
    public void consolidate() {
        final Collection<VehicleTraceAnalyzer> allVehicleTraceAnalyzers = traceAnalyzers.values();
        /** preparing steps */
        ScalarUnaryOperator any2target = UnitConvert.SI().to(TARGET_UNIT);
        allVehicleTraceAnalyzers.forEach(VehicleTraceAnalyzer::consolidate);
        /** calculation of values */
        // total distances driven per vehicle
        totalDistancesPerVehicle = Tensor.of(allVehicleTraceAnalyzers.stream().map(vs -> vs.vehicleTotalDistance)).map(any2target);
        // total distances
        totalDistance = Total.ofVector(totalDistancesPerVehicle);
        totalDistanceWtCst = (Scalar) Total.of(Tensor.of(allVehicleTraceAnalyzers.stream().map(vs -> vs.vehicleCustomerDist)).map(any2target));
        totalDistancePicku = (Scalar) Total.of(Tensor.of(allVehicleTraceAnalyzers.stream().map(vs -> vs.vehiclePickupDist)).map(any2target));
        totalDistanceRebal = (Scalar) Total.of(Tensor.of(allVehicleTraceAnalyzers.stream().map(vs -> vs.vehicleRebalancedist)).map(any2target));
        totalDistanceRatio = Scalars.lessThan(Quantity.of(0, TARGET_UNIT), totalDistance) ? //
                totalDistanceWtCst.divide(totalDistance) : RealScalar.of(-1);
        // distance per time of day
        distancesOverDay.append(Tensors.vector(0, 0, 0, 0));
        distanceRatioOverDay.append(RealScalar.ONE);
        for (int i = 1; i < times.size() - 1; ++i) {
            Long lTime = times.get(i - 1);
            Long uTime = times.get(i);
            Tensor stepDistance = Tensors.vector(0, 0, 0, 0);
            for (VehicleTraceAnalyzer tA : allVehicleTraceAnalyzers) {
                stepDistance = stepDistance.add(tA.labeledIntervalDistance(lTime, uTime));
            }
            distancesOverDay.append(stepDistance);
            Scalar distanceRatio = !Scalars.isZero(stepDistance.Get(0)) ? //
                    stepDistance.Get(1).divide(stepDistance.Get(0)) : RealScalar.ONE;
            if (Scalars.isZero(distanceRatio))
                distanceRatio = RealScalar.ZERO; // to remove units
            distanceRatioOverDay.append(distanceRatio);
        }
        distanceRatioOverDay.append(RealScalar.ONE);
        distancesOverDay.append(Tensors.vector(0, 0, 0, 0));
        /** average request distance */
        avgTripDistance = requestElement.reqsize() > 0 ? //
                totalDistanceWtCst.divide(RationalScalar.of(requestElement.reqsize(), 1)) : RealScalar.ZERO;
    }

    /** @return An unmodifiable List of all the Vehicle Statistics for all Vehicles
     *         in the fleet. */
    public List<VehicleTraceAnalyzer> getVehicleStatistics() {
        return List.copyOf(traceAnalyzers.values());
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCE, String.valueOf(totalDistance));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEPICKU, String.valueOf(totalDistancePicku));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEWTCST, String.valueOf(totalDistanceWtCst));
        map.put(TtlValIdent.TOTALROBOTAXIDISTANCEREB, String.valueOf(totalDistanceRebal));
        map.put(TtlValIdent.DISTANCERATIO, String.valueOf(totalDistanceRatio));
        map.put(TtlValIdent.AVGTRIPDISTANCE, String.valueOf(avgTripDistance));
        return map;
    }

    public static void setDistanceUnit(String unit) {
        setDistanceUnit(Unit.of(unit));
    }

    public static void setDistanceUnit(Unit unit) {
        if (CompatibleUnitQ.SI().with(SI.METER).test(Quantity.of(1, unit)))
            TARGET_UNIT = unit;
        else
            System.err.println("Unit " + unit + " is not a compatible distance unit!");
    }

    public static Unit getDistanceUnit() {
        return TARGET_UNIT;
    }
}
