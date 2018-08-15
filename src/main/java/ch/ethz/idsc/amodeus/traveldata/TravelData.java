/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.Serializable;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;

public class TravelData implements Serializable {
    private static final int DURATION = 24 * 60 * 60; // for now equal to one day
    private static final Clip TIME_CLIP = Clip.function(0, DURATION);
    // ---
    /** tensor (k,i,j) of dimension (numberofTimeSteps, numberVirtualNodes, numberVirtualNodes) that contains
     * the number of requests that come up in timeStep k from VS i to j */
    private final Tensor lambdaAbsolute;
    private final Tensor alphaAbsolute;
    private final Tensor v0;
    private final Tensor fAbsolute;
    private final int timeSteps;
    private final int timeInterval; // used as lookup
    private final long virtualNetworkID; // used for consistency check

    public TravelData(long virtualNetworkID, Tensor lambdaAbsolute, Tensor alphaAbsolute, Tensor fAbsolute, Tensor v0) {
        this.virtualNetworkID = virtualNetworkID;
        this.lambdaAbsolute = lambdaAbsolute;
        this.alphaAbsolute = alphaAbsolute;
        this.fAbsolute = fAbsolute;
        this.v0 = v0;
        this.timeSteps = lambdaAbsolute.length();
        this.timeInterval = DURATION / timeSteps;

        checkConsistency();
    }

    public Tensor getLambdaAbsoluteAtTime(int time) {
        GlobalAssert.that(TIME_CLIP.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeInterval).copy();
    }

    public Tensor getLambdaAbsolute() {
        return lambdaAbsolute.copy();
    }

    public Tensor getLambdaRateAtTime(int time) {
        GlobalAssert.that(TIME_CLIP.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeInterval).divide(RealScalar.of(timeInterval));
    }

    public Tensor getLambdaRate() {
        return lambdaAbsolute.divide(RealScalar.of(timeInterval));
    }

    public long getVirtualNetworkID() {
        return virtualNetworkID;
    }

    public int getTimeSteps() {
        return timeSteps;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public Tensor getAlphaAbsolute() {
        return alphaAbsolute.copy();
    }

    public Tensor getAlphaAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeInterval).copy();
    }

    public Tensor getAlphaRate() {
        return alphaAbsolute.divide(RealScalar.of(timeInterval));
    }

    public Tensor getAlphaRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeInterval).divide(RealScalar.of(timeInterval));
    }

    public Tensor getFAbsolute() {
        return fAbsolute.copy();
    }

    public Tensor getFAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeInterval).copy();
    }

    public Tensor getFRate() {
        return fAbsolute.divide(RealScalar.of(timeInterval));
    }

    public Tensor getFRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeInterval).divide(RealScalar.of(timeInterval));
    }

    public Tensor getV0() {
        return v0;
    }

    /** Perform consistency checks after completion of constructor operations. */
    public void checkConsistency() {
        GlobalAssert.that(lambdaAbsolute.flatten(-1).map(Scalar.class::cast).allMatch(Sign::isPositiveOrZero));
        Chop._06.close(lambdaAbsolute, Round.of(lambdaAbsolute)); // make sure lambdaAbsolute is integer valued
    }

    /** Checking if the virtualNetworkID's are identical
     * 
     * @param virtualNetworkID */
    public void checkIdenticalVirtualNetworkID(long virtualNetworkID) {
        GlobalAssert.that(DURATION % timeInterval == 0);
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(alphaAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(fAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(lambdaAbsolute).get(2)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(v0).get(0)));
        GlobalAssert.that(virtualNetworkID == this.virtualNetworkID);
    }
}