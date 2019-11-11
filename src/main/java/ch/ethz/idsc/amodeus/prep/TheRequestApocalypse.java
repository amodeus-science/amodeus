/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public final class TheRequestApocalypse {
    /** the seed is deliberately public */
    public static final long DEFAULT_SEED = 7582456789L;

    public static TheRequestApocalypse reducesThe(Population population) {
        return new TheRequestApocalypse(population);
    }

    // ---
    private final Population population;

    private TheRequestApocalypse(Population population) {
        this.population = population;
    }

    public TheRequestApocalypse toNoMoreThan(int maxRequests) {
        return toNoMoreThan(maxRequests, DEFAULT_SEED);
    }

    public TheRequestApocalypse toNoMoreThan(int maxRequests, long seed) {
        GlobalAssert.that(maxRequests <= LegCount.of(population, "av"));

        /** shuffle list of {@link Person}s */
        List<Person> list = new ArrayList<>(population.getPersons().values());
        Collections.shuffle(list, new Random(seed));
        Iterator<Person> iterator = list.iterator();

        // skip all persons that should completely remain in the population
        Person person = iterator.next();
        int totReq = 0;
        long req = LegCount.of(person, "av");
        while (totReq + req <= maxRequests) {
            totReq += req;
            person = iterator.next();
            req = LegCount.of(person, "av");
        }

        // create new person if needed to fill requests
        int split = maxRequests - totReq;
        if (split != 0) {
            Person splitPerson = SplitUp.of(population, person, split, "av");
            req = LegCount.of(splitPerson, "av");
            totReq += req;
            GlobalAssert.that(totReq == maxRequests);
            population.addPerson(splitPerson);
        }

        // remove all remaining persons
        iterator.forEachRemaining(p -> population.removePerson(p.getId()));
        GlobalAssert.that(LegCount.of(population, "av") == maxRequests);
        return this;
    }

    public final void requests() {
        System.out.println("Population size: " + population.getPersons().values().size());
    }
}
