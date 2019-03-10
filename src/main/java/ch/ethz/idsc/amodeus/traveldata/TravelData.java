/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import ch.ethz.idsc.tensor.Tensor;

public interface TravelData {

    /** Returns the absolute number of requests in the timeInterval where time is in. Lambda(i,j) is the number of requests that lead from virtual
     * station i to j. */
    Tensor getLambdaAbsoluteAtTime(int time);

    /** Returns the absolute number of requests. Lambda(k,i,j) is the number of requests that lead from virtual
     * station i to j in timeInterval k. */
    Tensor getLambdaAbsolute();

    /** Returns the request rates in the timeInterval where time is in. Lambda(i,j) is the request rate from virtual
     * station i to j. */
    Tensor getLambdaRateAtTime(int time);

    /** Returns the absolute rebalancing {@link Tensor}. Alpha(k,i,j) is the number of vehicles to rebalance from virtual
     * station i to j at timeInterval k. */
    Tensor getAlphaAbsolute();

    /** Returns the absolute rebalancing {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the number of vehicles to rebalance from virtual
     * station i to j in that certain timeInterval. */
    Tensor getAlphaAbsoluteAtTime(int time);

    /** Returns the rebalancing rate {@link Tensor}. Alpha(k,i,j) is the vehicles rate to rebalance from virtual
     * station i to j at timeInterval k. */
    Tensor getAlphaRate();

    /** Returns the rebalancing rate {@link Tensor} in the timeInterval that time is in. Alpha(i,j) is the vehicles rate to rebalance from virtual
     * station i to j in that certain timeInterval. */
    Tensor getAlphaRateAtTime(int time);

    /** Returns the absolute number of customer drives {@link Tensor}. F(k,i,j) represents the number of customer drives to do from virtual station i to j at
     * timeInterval k */
    Tensor getFAbsolute();

    /** Returns the absolute number of customer drives {@link Tensor} in the timeInterval that time is in. F(i,j) represents the number of customer drives to do
     * from virtual station i to j at the specific timeInterval */
    Tensor getFAbsoluteAtTime(int time);

    /** Returns the customer drive rates {@link Tensor}. F(k,i,j) represents customer drive rates to do from virtual station i to j at
     * timeInterval k */
    Tensor getFRate();

    /** Returns the customer drive rates {@link Tensor} in the timeInterval that time is in. F(i,j) represents the customer drive rates to do
     * from virtual station i to j at the specific timeInterval */
    Tensor getFRateAtTime(int time);

    /** Returns the lower bound of the initial vehicle distribution. */
    Tensor getV0();

    /** Returns the request rates. Lambda(k,i,j) is the request rate from virtual
     * station i to j in timeInterval k. */
    Tensor getLambdaRate();

    long getVirtualNetworkID();

    /** Returns the number of timeIntervals the day is split in. */
    int getTimeSteps();

    int getTimeIntervalLength();

    /** returns the name of the solver that was used to create travelData */
    String getLPName();

    /** returns true if {@link TravelData} covers this time */
    boolean coversTime(long round_now);

    /** Perform consistency checks after completion of constructor operations. */
    void checkConsistency();

    /** Checking if the virtualNetworkID's are identical
     * 
     * @param virtualNetworkID */
    void checkIdenticalVirtualNetworkID(long virtualNetworkID);

}