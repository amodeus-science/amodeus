/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

public enum LegCount {
    ;

    /** @return number of {@link Leg}s with @param mode in @param population */
    public static Scalar of(Population population, String mode) {
        Scalar reqCount = RealScalar.ZERO;
        for (Person person : population.getPersons().values()) {
            reqCount = reqCount.add(LegCount.of(person, mode));
        }
        return reqCount;
    }

    /** @return number of {@link Leg}s with @param mode in @param person */
    public static Scalar of(Person person, String mode) {
        int reqCount = 0;
        for (Plan plan : person.getPlans()) {
            for (PlanElement planelem : plan.getPlanElements()) {
                if (planelem instanceof Leg) {
                    Leg leg = (Leg) planelem;
                    if (leg.getMode().equals(mode))
                        ++reqCount;
                }
            }
        }
        return RealScalar.of(reqCount);
    }
}
