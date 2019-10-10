package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Chop;

public class LinkSpeedTestSmall {

    private List<TrafficDelayEstimate> calculators;

    @Before
    public void prepare() {
        calculators = new ArrayList<>();
        calculators.add(GLPKLinOptDelayCalculator.INSTANCE);

    }

    @Test
    public void trafficFlowChangeTest() throws Exception {
        for (TrafficDelayEstimate calculator : calculators) {
            Tensor flowMatrix = Tensors.fromString("{{1,1,0,0},{0,0,1,1},{0,0,1,1}}");
            Tensor deviationMatrix = Tensors.fromString("{{0.3},{0.4},{0.4}}");
            System.out.println("Using: " + calculator.getClass().getSimpleName());
            Tensor trafficFlowChange = calculator.compute(flowMatrix, deviationMatrix);
            System.out.println("trafficFlowChange: " + trafficFlowChange);
            Tensor difference = trafficFlowChange//
                    .subtract(Tensors.fromString("{{0.1499999999999999}, {0.14999999999999997},"//
                            + " {0.19999999999999996},{0.19999999999999996}}"));
            if (!(calculator instanceof GLPKLinOptDelayCalculator))
                Assert.assertTrue(Chop._15.of(Norm._2.of(difference)).equals(RealScalar.ZERO));
        }

    }

    @Test
    public void trafficFlowAbsTest() throws Exception {
        for (TrafficDelayEstimate calculator : calculators) {
            Tensor flowMat = Tensors.fromString("{{1,1,0,0},{0,0,1,1},{0,0,1,1}}");
            Tensor travelTimeFree = Tensors.fromString("{{2},{2},{2}}");
            Tensor travelTimeMeasure = Tensors.fromString("{{2.3},{2.4},{2.4}}");
            FlowTrafficEstimation estimator = //
                    FlowTrafficEstimation.of(flowMat, travelTimeFree, travelTimeMeasure, calculator);
            Tensor travelTimeEstimate = estimator.trafficTravelTimeEstimates();
            if (!(calculator instanceof GLPKLinOptDelayCalculator)) {
                Assert.assertTrue(travelTimeEstimate.equals(Tensors.fromString("{{2.3}, {2.4}, {2.4}}")));
                Assert.assertTrue(Norm._2.of(travelTimeEstimate.subtract(travelTimeMeasure)).equals(RealScalar.ZERO));
                Assert.assertTrue(travelTimeEstimate.Get(1, 0).equals(RealScalar.of(2.4)));
                Assert.assertTrue(estimator.getError().equals(RealScalar.ZERO));
            }
        }
    }

}
