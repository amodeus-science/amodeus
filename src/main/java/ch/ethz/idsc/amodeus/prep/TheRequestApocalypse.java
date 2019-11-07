/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

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

    // TODO why is maxRequests a scalar and not an integer?

    public TheRequestApocalypse toNoMoreThan(Scalar maxRequests) {
        return toNoMoreThan(maxRequests, DEFAULT_SEED);
    }

    public TheRequestApocalypse toNoMoreThan(Scalar maxRequests, long seed) {
        GlobalAssert.that(Scalars.lessEquals(maxRequests, LegCount.of(population, "av")));

        /** shuffle list of {@link Person}s */
        List<Person> list = new ArrayList<>(population.getPersons().values());
        Collections.shuffle(list, new Random(seed));
        Iterator<Person> iterator = list.iterator();

        // skip all persons that should completely remain in the population
        Person person = iterator.next();
        Scalar totReq = RealScalar.ZERO;
        Scalar req = LegCount.of(person, "av");
        while (Scalars.lessEquals(totReq.add(req), maxRequests)) {
            totReq = totReq.add(req);
            person = iterator.next();
            req = LegCount.of(person, "av");
        }

        // create new person if needed to fill requests
        Scalar split = maxRequests.subtract(totReq);
        if (!split.equals(RealScalar.ZERO)) {
            Person splitPerson = SplitUp.of(population, person, split, "av");
            req = LegCount.of(splitPerson, "av");
            totReq = totReq.add(req);
            GlobalAssert.that(totReq.equals(maxRequests));
            population.addPerson(splitPerson);
        }

        // remove all remaining persons
        iterator.forEachRemaining(p -> population.removePerson(p.getId()));
        GlobalAssert.that(LegCount.of(population, "av").equals(maxRequests));
        return this;
    }

    public final void requests() {
        System.out.println("Population size: " + population.getPersons().values().size());
    }
}
