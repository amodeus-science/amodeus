/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.HashSet;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.ExactScalarQ;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/* package */ enum CommandConsistency {
    ;

    /** Performs the following consistency checks on the commands received and fails if >=1
     * check is failing:
     * 
     * 1) ensure every RoboTaxi should be in {0,1} of the commands
     * 2) ensure every request should only be assigned {0,1} times
     * 
     * @param commands */
    public static void check(Tensor commands) {

        GlobalAssert.that(ExactScalarQ.all(commands));

        /** 1) ensure every RoboTaxi should be in {0,1} of the commands
         ** 2) ensure every request should only be assigned {0,1} times */
        Set<Scalar> usdRobTaxis = new HashSet<>();
        Set<Scalar> usedPickups = new HashSet<>();

        Tensor pickups = commands.get(0);
        for (Tensor pickup : pickups) {
            GlobalAssert.that(usdRobTaxis.add(pickup.Get(0)));
            GlobalAssert.that(usedPickups.add(pickup.Get(1)));
        }

        Tensor rebalances = commands.get(1);
        for (Tensor rebalance : rebalances)
            GlobalAssert.that(usdRobTaxis.add(rebalance.Get(0)));

    }

}
