/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

public enum LegCount {
    ;

    /** @return number of {@link Leg}s with @param mode in @param population */
    public static long of(Population population, String mode) {
        return population.getPersons().values().stream().map(person -> LegCount.of(person, mode)).reduce(Long::sum).orElse(0L);
    }

    /** @return number of {@link Leg}s with @param mode in @param person */
    public static long of(Person person, String mode) {
        return person.getPlans().stream().flatMap(plan -> //
        plan.getPlanElements().stream().filter(pe -> pe instanceof Leg).map(pe -> (Leg) pe) // for each plan get stream of Legs
        ).map(Leg::getMode).filter(m -> m.equals(mode)).count(); // count all Legs with mode from all streams
    }
}
