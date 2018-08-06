/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Clip;

@SuppressWarnings("serial")
public class RebalanceData implements Serializable {
    private static final int DURATION = 24 * 60 * 60; // for now equal to one day
    private static final Clip TIME_CLIP = Clip.function(0, DURATION);
    // ---
    private final Tensor alphaAbsolute;
    private final Tensor v0_i;
    private final Tensor fAbsolute;
    private final int timeInterval;
    private final double LPweightR;
    private final double LPweightQ;
    private final String solver;

    public RebalanceData(LPSolver solver, ScenarioOptions scenarioOptions) {
        alphaAbsolute = solver.getAlphaAbsolute_ij();
        fAbsolute = solver.getFAbsolute_ij();
        v0_i = solver.getV0_i();
        timeInterval = solver.getTimeInterval();
        this.solver = solver.getClass().getSimpleName();
        LPweightR = scenarioOptions.getLPWeightR();
        LPweightQ = scenarioOptions.getLPWeightQ();
    }

    public Tensor getAlphaAbsolute() {
        return alphaAbsolute.copy();
    }

    public Tensor getAlphaAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeInterval).copy();
    }

    public Tensor getAlphaRate() {
        return alphaAbsolute.multiply(RealScalar.of(timeInterval).reciprocal());
    }

    public Tensor getAlphaRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeInterval).multiply(RealScalar.of(timeInterval).reciprocal());
    }

    public Tensor getFAbsolute() {
        return fAbsolute.copy();
    }

    public Tensor getFAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeInterval).copy();
    }

    public Tensor getFRate() {
        return fAbsolute.multiply(RealScalar.of(timeInterval).reciprocal());
    }

    public Tensor getFRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeInterval).multiply(RealScalar.of(timeInterval).reciprocal());
    }

    public Tensor getV0() {
        return v0_i;
    }

    public double getLPweightR() {
        return LPweightR;
    }

    public double getLPweightQ() {
        return LPweightQ;
    }

    public String getSolver() {
        return solver;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public int getTimeSteps() {
        return DURATION / timeInterval;
    }

    public void export(ScenarioOptions scenarioOptions) throws IOException {
        RebalanceDataIO.write(new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getRebalanceDataName()), this);
    }

}
