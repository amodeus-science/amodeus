/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** This {@link Id} generator takes the set of usedIDs in the constructor
 * and then finds the largest integer value in these IDs, every call of generateUnusedID
 * then creates an id idName + largestInteger +i where largestInteger is initialized with the largest
 * found integer */
public class IDGenerator {

    private final Set<Id<Person>> usedIDs;
    private Integer largestInteger;
    private final String idName = "IDGenerator";
    private final String format = "%012d";

    public IDGenerator(Population populationExisting) {
        this.usedIDs = getUsedIDs(populationExisting);

        // find largest integer used in IDs
        List<Integer> foundInts = new ArrayList<>();
        for (Id<Person> id : usedIDs) {
            foundInts.add(extractLargestInt(id.toString()));
        }
        Collections.sort(foundInts);
        if (foundInts.get(foundInts.size() - 1) != null) {
            largestInteger = foundInts.get(foundInts.size() - 1);
        } else {
            largestInteger = 1;
        }
    }

    /** @param usedIDs
     * @return new ID which is not yet in set usedIDs */
    public Id<Person> generateUnusedID() {
        largestInteger++;
        String newIDs = idName + String.format(format, largestInteger);
        Id<Person> newId = Id.create(newIDs, Person.class);
        usedIDs.add(newId);
        return newId;
    }

    /** @param str
     * @return the highest number in str */
    public Integer extractLargestInt(final String str) {
        // collects all strings that are contained in str (use non-digit strings as delimiter)
        String[] integerStrings = str.split("\\D+");
        List<Integer> integerList = new ArrayList<>();
        // convert integerStrings to real integers and add them to integerList
        Arrays.stream(integerStrings).filter(v -> v.length() > 0).forEach(v -> integerList.add(Integer.parseInt(v)));

        return Collections.max(integerList);
    }

    /** @param populationExisting
     * @return HashSet<Id<Person>> containing all used IDs in @param populationExisting */
    public Set<Id<Person>> getUsedIDs(Population populationExisting) {
        HashSet<Id<Person>> usedIDs = new HashSet<>();
        populationExisting.getPersons().values().forEach(p -> usedIDs.add(p.getId()));
        GlobalAssert.that(usedIDs.size() == populationExisting.getPersons().size());
        return usedIDs;
    }
}
