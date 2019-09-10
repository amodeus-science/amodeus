package ch.ethz.idsc.amodeus.linkspeed.create;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.idsc.amodeus.linkspeed.create.GLPKLinOptDelayCalculator;
import ch.ethz.idsc.amodeus.linkspeed.create.LSQTrafficEstimation;
import ch.ethz.idsc.amodeus.linkspeed.create.TrafficDelayEstimate;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.sca.Round;

/* package */ class LinkSpeedTestLarge {

    private List<TrafficDelayEstimate> calculators;

    @Test
    public void largeSizeTest() throws Exception {
        int severity = 100;
        int variables = (int) (3.0400 * severity); // GOAL is factor 10'000 without heap problems
        int equations = (int) (1.7000 * severity); // GOAL is factor 10'000 without heap problems
        Random random = new Random(12345689);

        /** setting up zero matrices, flowmatrix with zero, 1 entries */
        System.out.println("seeting up matrices");
        Tensor deviationMatrix = Array.zeros(equations, 1);
        Tensor freeflowMatrix = Array.zeros(equations, 1);
        Tensor flowMatrix = Array.zeros(equations, variables);
        System.out.println("seeting up matrices done");

        System.out.println("added random values");
        deviationMatrix = Transpose.of(Tensors.of(deviationMatrix));
        freeflowMatrix = Transpose.of(Tensors.of(freeflowMatrix));
        System.out.println("before entering lsqcalc");
        // System.out.println(Pretty.of(deviationMatrix));
        // System.out.println(Pretty.of(flowMatrix));

        /** filling with random elements */
        for (int i = 0; i < equations; ++i) {
            if (i % 100 == 0)
                System.out.println("i: " + i);
            double sign = random.nextBoolean() ? -1.0 : 1.0;
            deviationMatrix.set(RealScalar.of(random.nextDouble() * sign), i);
            freeflowMatrix.set(RealScalar.of(random.nextDouble()), i);
            for (int j = 0; j < variables; ++j) {
                flowMatrix.set(Round.of(RealScalar.of(random.nextDouble())), i, j);
            }
        }

        Tensor trafficTimeMatrix = freeflowMatrix.add(deviationMatrix);

        trafficTimeMatrix = Transpose.of(Tensors.of(trafficTimeMatrix));
        freeflowMatrix = Transpose.of(Tensors.of(freeflowMatrix));

        for (TrafficDelayEstimate calculator : calculators) {
            System.err.println(calculator.getClass().getSimpleName());
            /** TensorDelayCalculator is skipped as too slow */
            long time = System.currentTimeMillis();
            LSQTrafficEstimation estimator = //
                    LSQTrafficEstimation.of(flowMatrix, freeflowMatrix, trafficTimeMatrix, calculator);
            Tensor travelTimeEstimate = estimator.trafficTravelTimeEstimates;
            System.out.println("travelTimeEstimate:" + travelTimeEstimate);
            System.out.println("duration: [s] " + (System.currentTimeMillis() - time) / 1000);
            /** if we get here without an error, then it is fine */
            Assert.assertTrue(true);
        }
    }

    @Before
    public void prepare() {
        calculators = new ArrayList<>();
        calculators.add(GLPKLinOptDelayCalculator.INSTANCE);

    }
}
