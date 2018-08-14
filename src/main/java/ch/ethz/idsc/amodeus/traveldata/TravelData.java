/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import java.io.Serializable;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.prep.PopulationTools;
import ch.ethz.idsc.amodeus.prep.Request;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;

public class TravelData implements Serializable {
    private static final int DURATION = 24 * 60 * 60; // for now equal to one day
    private static final Clip TIME_CLIP = Clip.function(0, DURATION);
    // ---
    /** tensor (i,j,k) of dimension (numberofTimeSteps, numberVirtualNodes, numberVirtualNodes) that contains
     * the number of requests that come up in timeStep k from VS i to j */
    private final Tensor lambdaAbsolute;
    private final int timeSteps;
    private final int timeInterval; // used as lookup
    private final long virtualNetworkID; // used for consistency check

    /** Constructor for TravelData object creating historic travel information based on virtualNetwork and population
     * 
     * @param virtualNetworkIn
     * @param network
     * @param population
     * @param timeInterval has to be a divider of the DURATION (i.e. 24*3600)
     * @param ratio is the scaling factor, e.g. ratio = 0.1 if lambda is created with 10'000 trips but only 1'000 are actually simulated */
    public TravelData(VirtualNetwork<Link> virtualNetwork, Network network, Population population, int timeInterval, Scalar ratio) {
        System.out.println("reading travel data for population of size " + population.getPersons().size());
        virtualNetworkID = virtualNetwork.getvNetworkID();
        System.out.println("the ID of the virtualNetwork used for travel data construction is: " + virtualNetworkID);

        this.timeInterval = timeInterval;
        GlobalAssert.that(DURATION % timeInterval == 0);
        timeSteps = DURATION / timeInterval;
        System.out.println("Number of time steps = " + timeSteps);

        // create the lambda Tensors
        Set<Request> avRequests = PopulationTools.getAVRequests(population, network);

        Tensor lambdaTotal = PopulationTools.getLambdaInVirtualNodesAndTimeIntervals(avRequests, virtualNetwork, timeInterval);

        lambdaAbsolute = Round.of(lambdaTotal.multiply(ratio));
        checkConsistency();
    }

    public TravelData(VirtualNetwork<Link> virtualNetwork, Network network, Population population, int timeInterval) {
        this(virtualNetwork, network, population, timeInterval, RealScalar.ONE);
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
        return lambdaAbsolute.get(time / timeInterval).multiply(RealScalar.of(timeInterval).reciprocal());
    }

    public Tensor getLambdaRate() {
        return lambdaAbsolute.multiply(RealScalar.of(timeInterval).reciprocal());
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

    /** Perform consistency checks after completion of constructor operations. */
    public void checkConsistency() {
        GlobalAssert.that(lambdaAbsolute.flatten(-1).map(Scalar.class::cast).allMatch(Sign::isPositiveOrZero));
        Chop._06.close(lambdaAbsolute, Round.of(lambdaAbsolute)); // make sure lambdaAbsolute is integer valued
    }

    /** Checking if the virtualNetworkID's are identical
     * 
     * @param virtualNetworkID */
    public void checkIdenticalVirtualNetworkID(long virtualNetworkID) {
        GlobalAssert.that(virtualNetworkID == this.virtualNetworkID);
    }
}