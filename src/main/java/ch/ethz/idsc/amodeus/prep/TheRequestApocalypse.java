/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
        List<Id<Person>> list = new ArrayList<>(population.getPersons().keySet());
        Collections.shuffle(list, new Random(seed));

        /** doc */
        Scalar totReq = RealScalar.ZERO;
        Set<Id<Person>> keepList = new HashSet<>();
        Person splitUpPerson = null;

        for (Id<Person> pId : list) {
            if (totReq.equals(maxRequests)) // TODO totReq == maxRequests -> totReq >= maxRequests?
                break;
            Scalar req = LegCount.of(population.getPersons().get(pId), "av");
            if (Scalars.lessEquals(totReq.add(req), maxRequests)) {
                totReq = totReq.add(req);
                keepList.add(pId);
            } else { // adding more than
                Scalar splitNeeded = maxRequests.subtract(totReq);
                splitUpPerson = SplitUp.of(population, population.getPersons().get(pId), splitNeeded, "av");
                req = LegCount.of(splitUpPerson, "av");
                GlobalAssert.that(totReq.add(req).equals(maxRequests));
                totReq = totReq.add(req); // TODO updated but necessary added to population?
            }
        }

        list.stream().filter(pId -> !keepList.contains(pId)).forEach(population::removePerson);

        if (Objects.nonNull(splitUpPerson))
            population.addPerson(splitUpPerson);

        GlobalAssert.that(LegCount.of(population, "av").equals(maxRequests));
        return this;
    }

    public final void requests() {
        System.out.println("Population size: " + population.getPersons().values().size());
    }
}
