/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

/** This {@link Id} generator takes the set of usedIDs in the constructor
 * and then finds the largest integer value in these IDs, every call of generateUnusedID
 * then creates an id idName + largestInteger +i where largestInteger is initialized with the largest
 * found integer */
public class IDGenerator {
    private final Set<Id<Person>> usedIDs;
    private final String idName = "IDGenerator";
    private final String format = "%012d";
    private Integer largestInteger;

    public IDGenerator(Population populationExisting) {
        this.usedIDs = populationExisting.getPersons().keySet();

        // find largest integer used in IDs
        largestInteger = usedIDs.stream().map(Id::toString).map(this::extractLargestInt) //
                .filter(OptionalInt::isPresent).mapToInt(OptionalInt::getAsInt).max().orElse(1);
    }

    /** @return new ID which is not yet in set usedIDs */
    public Id<Person> generateUnusedID() {
        largestInteger++;
        String newIDs = idName + String.format(format, largestInteger);
        Id<Person> newId = Id.create(newIDs, Person.class);
        usedIDs.add(newId);
        return newId;
    }

    /** @param str person id
     * @return the highest number in str */
    public OptionalInt extractLargestInt(final String str) {
        // collects all strings that are contained in str (use non-digit strings as delimiter)
        String[] integerStrings = str.split("\\D+");
        return Arrays.stream(integerStrings).filter(v -> !v.isEmpty()).mapToInt(Integer::parseInt).max();
    }
}
