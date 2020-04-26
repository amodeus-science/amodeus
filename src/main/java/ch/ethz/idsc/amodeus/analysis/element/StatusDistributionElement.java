/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class StatusDistributionElement implements AnalysisElement, TotalValueAppender {

    public final Tensor time = Tensors.empty();
    public final Tensor statusTensor = Tensors.empty();
    public final Tensor occupancyTensor = Tensors.empty();

    @Override // from AnalysisElement
    public void register(SimulationObject simulationObject) {

        /** Get the TimeStep */
        time.append(RealScalar.of(simulationObject.now));

        /** Get the Status Distribution per TimeStep */
        Tensor numStatus = StaticHelper.getNumStatus(simulationObject);
        statusTensor.append(numStatus);

        /** Get the Occupancy Ratio per TimeStep */
        Scalar occupancyRatio = numStatus.Get(RoboTaxiStatus.DRIVEWITHCUSTOMER.ordinal()) //
                .divide(RealScalar.of(simulationObject.vehicles.size()));
        occupancyTensor.append(Tensors.vector(simulationObject.now, occupancyRatio.number().doubleValue()));
    }

    @Override // from AnalysisElement
    public void consolidate() {
        int timeStep = time.Get(1).subtract(time.Get(0)).number().intValue();
        // Map<RoboTaxiStatus, Integer> timeStepsPerStatus = new HashMap<>();
        // for (RoboTaxiStatus roboTaxiStatus : RoboTaxiStatus.values())
        //     timeStepsPerStatus.put(roboTaxiStatus, getTimeStepsInStatus(roboTaxiStatus));
        // alternative
        //Map<RoboTaxiStatus, Integer> timeStepsPerStatus = Arrays.stream(RoboTaxiStatus.values()).collect(Collectors.toMap(Function.identity(), this::getTimeStepsInStatus));

        @SuppressWarnings("unused") // TODO add to total values or remove entirely
        // double totalDriveTime = timeStepsPerStatus.entrySet().stream() //
        //         .filter(e -> e.getKey().isDriving()) //
        //         .mapToDouble(Entry::getValue) //
        //         .sum() * timeStep;
        double totalDriveTime = (double) timeStep * //
                Arrays.stream(RoboTaxiStatus.values()).filter(RoboTaxiStatus::isDriving).map(this::getTimeStepsInStatus).reduce(Integer::sum).get();
        // totalValues.put(TotalValueIdentifiersAmodeus.TOTALROBOTAXIDRIVETIME,
        // String.valueOf(totalDriveTime));
    }

    private int getTimeStepsInStatus(RoboTaxiStatus roboTaxiStatus) {
        return statusTensor.get(Tensor.ALL, roboTaxiStatus.ordinal()).stream().reduce(Tensor::add).get().Get().number().intValue();
    }

    @Override // from TotalValueAppender
    public Map<TotalValueIdentifier, String> getTotalValues() {
        return Collections.emptyMap();
    }
}
