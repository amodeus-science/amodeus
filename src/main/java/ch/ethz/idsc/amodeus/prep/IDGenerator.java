/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<Integer> foundInts = usedIDs.stream().map(Id::toString).map(this::extractLargestInt).sorted().collect(Collectors.toList());
        largestInteger = Optional.ofNullable(foundInts.get(foundInts.size() - 1)).orElse(1);
        // TODO confirm return value null and fallback 1 alternatively:
        // largestInteger = usedIDs.stream().map(Id::toString).mapToInt(this::extractLargestInt).max().orElse(1);
    }

    /** @return new ID which is not yet in set usedIDs */
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
        return Arrays.stream(integerStrings).filter(v -> !v.isEmpty()).mapToInt(Integer::parseInt).max().orElseThrow(RuntimeException::new);
    }

    /** @param populationExisting
     * @return HashSet<Id<Person>> containing all used IDs in @param populationExisting */
    public Set<Id<Person>> getUsedIDs(Population populationExisting) {
        // TODO is this necessary?
        // populationExisting.getPersons() -> Map<Id<Person>>, ? extends Person> hence this always returns the key set, also making the check redundant
        // PopulationImpl seems to be the only place where Population::getPersons() is set
        Set<Id<Person>> usedIDs = populationExisting.getPersons().values().stream().map(Person::getId).collect(Collectors.toSet());
        GlobalAssert.that(usedIDs.size() == populationExisting.getPersons().size());
        return usedIDs;
    }
}
