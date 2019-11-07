/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

public enum LegCount {
    ;

    /** @return number of {@link Leg}s with @param mode in @param population */
    public static Scalar of(Population population, String mode) {
        return population.getPersons().values().stream().map(person -> LegCount.of(person, mode)).reduce(Scalar::add).orElse(RealScalar.ZERO);
    }

    /** @return number of {@link Leg}s with @param mode in @param person */
    public static Scalar of(Person person, String mode) {
        return RealScalar.of(person.getPlans().stream().flatMap(plan -> //
                plan.getPlanElements().stream().filter(pe -> pe instanceof Leg).map(pe -> (Leg) pe) // for each plan get stream of Legs
        ).map(Leg::getMode).filter(m -> m.equals(mode)).count()); // count all Legs with mode from all streams
    }
}
