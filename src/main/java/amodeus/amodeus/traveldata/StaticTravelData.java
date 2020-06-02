/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.traveldata;

import java.io.Serializable;

import amodeus.amodeus.prep.VirtualNetworkPreparer;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;

/** Contains the request informations, the rebalancing information from an LPsolver,
 * the initial vehicle distribution requirements and the number of customer drives.
 * 
 * In order to use TravelData, it has to be created in the Preparer by {@link VirtualNetworkPreparer},
 * then it can be injected in the Server by {@link VirtualNetworkModule}.
 * 
 * TravelData is also used in tests. */
public class StaticTravelData implements TravelData, Serializable {
    private final Clip timeClip;
    /** tensor (k,i,j) of dimension (numberofTimeSteps, numberVirtualNodes, numberVirtualNodes) that contains
     * the number of requests that come up in timeStep k from VS i to j */
    private final Tensor lambdaAbsolute;
    private final Tensor alphaAbsolute;
    private final Tensor v0;
    private final Tensor fAbsolute;
    private final String lpName;
    private final int timeSteps;
    private final int timeIntervalLength; // used as lookup
    private final int endTime; // in [s]
    private final long virtualNetworkID; // used for consistency check

    public StaticTravelData(long virtualNetworkID, Tensor lambdaAbsolute, Tensor alphaAbsolute, Tensor fAbsolute, Tensor v0, String lpName, int endTime) {
        this.virtualNetworkID = virtualNetworkID;
        this.lambdaAbsolute = lambdaAbsolute;
        this.alphaAbsolute = alphaAbsolute;
        this.fAbsolute = fAbsolute;
        this.v0 = v0;
        this.timeSteps = lambdaAbsolute.length();
        this.timeIntervalLength = endTime / timeSteps;
        this.endTime = endTime;
        this.lpName = lpName;
        timeClip = Clips.positive(endTime - 1);

        checkConsistency();
    }

    /** Returns the absolute number of requests in the timeInterval where time is in. Lambda(i,j) is the number of requests that lead from virtual
     * station i to j. */
    @Override
    public Tensor getLambdaAbsoluteAtTime(int time) {
        GlobalAssert.that(timeClip.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the absolute number of requests. Lambda(k,i,j) is the number of requests that lead from virtual
     * station i to j in timeInterval k. */
    @Override
    public Tensor getLambdaAbsolute() {
        return lambdaAbsolute.copy();
    }

    /** Returns the request rates in the timeInterval where time is in. Lambda(i,j) is the request rate from virtual
     * station i to j. */
    @Override
    public Tensor getLambdaRateAtTime(int time) {
        GlobalAssert.that(timeClip.isInside(RealScalar.of(time)));
        return lambdaAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the absolute rebalancing {@link Tensor}. Alpha(k,i,j) is the number of vehicles to rebalance from virtual
     * station i to j at timeInterval k. */
    @Override
    public Tensor getAlphaAbsolute() {
        return alphaAbsolute.copy();
    }

    /** Returns the absolute rebalancing {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the number of vehicles to rebalance from virtual
     * station i to j in that certain timeInterval. */
    @Override
    public Tensor getAlphaAbsoluteAtTime(int time) {
        timeClip.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the rebalancing rate {@link Tensor}. Alpha(k,i,j) is the vehicles rate to rebalance from virtual
     * station i to j at timeInterval k. */
    @Override
    public Tensor getAlphaRate() {
        return alphaAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the rebalancing rate {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the vehicles rate to rebalance from virtual
     * station i to j in that certain timeInterval. */
    @Override
    public Tensor getAlphaRateAtTime(int time) {
        timeClip.requireInside(RealScalar.of(time));
        return alphaAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the absolute number of customer drives {@link Tensor}. F(k,i,j) represents the number of customer drives to do from virtual station i to j at
     * timeInterval k */
    @Override
    public Tensor getFAbsolute() {
        return fAbsolute.copy();
    }

    /** Returns the absolute number of customer drives {@link Tensor} in the timeInterval that time is in. F(i,j) represents the number of customer drives to do
     * from virtual station i to j at the specific timeInterval */
    @Override
    public Tensor getFAbsoluteAtTime(int time) {
        timeClip.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeIntervalLength).copy();
    }

    /** Returns the customer drive rates {@link Tensor}. F(k,i,j) represents customer drive rates to do from virtual station i to j at
     * timeInterval k */
    @Override
    public Tensor getFRate() {
        return fAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the customer drive rates {@link Tensor} in the timeInterval that time is in. F(i,j) represents the customer drive rates to do
     * from virtual station i to j at the specific timeInterval */
    @Override
    public Tensor getFRateAtTime(int time) {
        timeClip.requireInside(RealScalar.of(time));
        return fAbsolute.get(time / timeIntervalLength).divide(RealScalar.of(timeIntervalLength));
    }

    /** Returns the lower bound of the initial vehicle distribution. */
    @Override
    public Tensor getV0() {
        return v0;
    }

    /** Returns the request rates. Lambda(k,i,j) is the request rate from virtual
     * station i to j in timeInterval k. */
    @Override
    public Tensor getLambdaRate() {
        return lambdaAbsolute.divide(RealScalar.of(timeIntervalLength));
    }

    @Override
    public long getVirtualNetworkID() {
        return virtualNetworkID;
    }

    /** Returns the number of timeIntervals the day is split in. */
    @Override
    public int getTimeSteps() {
        return timeSteps;
    }

    @Override
    public int getTimeIntervalLength() {
        return timeIntervalLength;
    }

    /** returns the name of the solver that was used to create travelData */
    @Override
    public String getLPName() {
        return lpName;
    }

    /** returns true if {@link TravelData} covers this time */
    @Override
    public boolean coversTime(long round_now) {
        return timeClip.isInside(RealScalar.of(round_now));
    }

    /** Perform consistency checks after completion of constructor operations. */
    @Override
    public void checkConsistency() {
        GlobalAssert.that(lambdaAbsolute.flatten(-1).map(Scalar.class::cast).allMatch(Sign::isPositiveOrZero));
        Chop._06.close(lambdaAbsolute, Round.of(lambdaAbsolute)); // make sure lambdaAbsolute is integer valued
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(alphaAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).equals(Dimensions.of(fAbsolute)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(lambdaAbsolute).get(2)));
        GlobalAssert.that(Dimensions.of(lambdaAbsolute).get(1).equals(Dimensions.of(v0).get(0)));
    }

    /** Checking if the virtualNetworkID's are identical
     * 
     * @param virtualNetworkID */
    @Override
    public void checkIdenticalVirtualNetworkID(long virtualNetworkID) {
        GlobalAssert.that(endTime % timeIntervalLength == 0);
        GlobalAssert.that(virtualNetworkID == this.virtualNetworkID);
    }
}