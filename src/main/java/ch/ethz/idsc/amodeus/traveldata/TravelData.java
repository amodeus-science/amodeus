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

/** Contains the request informations, the rebalancing information from an LPsolver, the initial vehicle distribution requirements and the number of customer
 * drives */
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
    private final int timeIntervalLength; // used as lookup
    private final long virtualNetworkID; // used for consistency check

    public TravelData(long virtualNetworkID, Tensor lambdaAbsolute, Tensor alphaAbsolute, Tensor fAbsolute, Tensor v0) {
        this.virtualNetworkID = virtualNetworkID;
        this.lambdaAbsolute = lambdaAbsolute;
        this.alphaAbsolute = alphaAbsolute;
        this.fAbsolute = fAbsolute;
        this.v0 = v0;
        this.timeSteps = lambdaAbsolute.length();
        this.timeIntervalLength = DURATION / timeSteps;

        checkConsistency();
    }

    /** Returns the absolute number of requests in the timeInterval where time is in. Lambda(i,j) is the number of requests that lead from virtual
     * station i to j. */
    public Tensor getLambdaAbsoluteAtTime(int time) {
        GlobalAssert.that(TIME_CLIP.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the absolute number of requests. Lambda(k,i,j) is the number of requests that lead from virtual
     * station i to j in timeInterval k. */
    public Tensor getLambdaAbsolute() {
        return lambdaAbsolute.copy();
    }

    /** Returns the request rates in the timeInterval where time is in. Lambda(i,j) is the request rate from virtual
     * station i to j. */
    public Tensor getLambdaRateAtTime(int time) {
        GlobalAssert.that(TIME_CLIP.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the absolute rebalancing {@link Tensor}. Alpha(k,i,j) is the number of vehicles to rebalance from virtual
     * station i to j at timeInterval k. */
    public Tensor getAlphaAbsolute() {
        return alphaAbsolute.copy();
    }

    /** Returns the absolute rebalancing {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the number of vehicles to rebalance from virtual
     * station i to j in that certain timeInterval. */
    public Tensor getAlphaAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the rebalancing rate {@link Tensor}. Alpha(k,i,j) is the vehicles rate to rebalance from virtual
     * station i to j at timeInterval k. */
    public Tensor getAlphaRate() {
        return alphaAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the rebalancing rate {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the vehicles rate to rebalance from virtual
     * station i to j in that certain timeInterval. */
    public Tensor getAlphaRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the absolute number of customer drives {@link Tensor}. F(k,i,j) represents the number of customer drives to do from virtual station i to j at
     * timeInterval k */
    public Tensor getFAbsolute() {
        return fAbsolute.copy();
    }

    /** Returns the absolute number of customer drives {@link Tensor} in the timeInterval that time is in. F(i,j) represents the number of customer drives to do
     * from virtual station i to j at the specific timeInterval */
    public Tensor getFAbsoluteAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the customer drive rates {@link Tensor}. F(k,i,j) represents customer drive rates to do from virtual station i to j at
     * timeInterval k */
    public Tensor getFRate() {
        return fAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the customer drive rates {@link Tensor} in the timeInterval that time is in. F(i,j) represents the customer drive rates to do
     * from virtual station i to j at the specific timeInterval */
    public Tensor getFRateAtTime(int time) {
        TIME_CLIP.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the lower bound of the initial vehicle distribution. */
    public Tensor getV0() {
        return v0;
    }

    /** Returns the request rates. Lambda(k,i,j) is the request rate from virtual
     * station i to j in timeInterval k. */
    public Tensor getLambdaRate() {
        return lambdaAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    public long getVirtualNetworkID() {
        return virtualNetworkID;
    }

    /** Returns the number of timeIntervals the day is split in. */
    public int getTimeSteps() {
        return timeSteps;
    }

    public int getTimeIntervalLength() {
        return timeIntervalLength;
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
        GlobalAssert.that(DURATION % timeIntervalLength == 0);
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(alphaAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(fAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(lambdaAbsolute).get(2)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(v0).get(0)));
        GlobalAssert.that(virtualNetworkID == this.virtualNetworkID);
    }
}