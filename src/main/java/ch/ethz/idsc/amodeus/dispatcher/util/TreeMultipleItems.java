/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TreeMultipleItems<T> {

    private final NavigableMap<Double, Set<T>> tree = new TreeMap<>();
    /** data structures are used to enable fast "contains" searching */
    private final Set<T> set = new HashSet<>();
    private final Function<T, Double> function;

    public TreeMultipleItems(Function<T, Double> function) {
        this.function = function;
    }

    /** @return Set of {@link T} associated with the lowest value obtained from the function */
    public Set<T> getFirst() {
        return returnIfPossible(tree.firstEntry());
    }

    /** @return Set of {@link T} associated with the highest value obtained from the function */
    public Set<T> getLast() {
        return returnIfPossible(tree.lastEntry());
    }

    private Set<T> returnIfPossible(Entry<Double, Set<T>> entry) {
        return Optional.ofNullable(entry).map(Entry::getValue).orElse(null);
    }

    public List<T> getTsInOrderOfValue() {
        return tree.values().stream().flatMap(Set::stream).collect(Collectors.toList()); // is ascending order ?!
    }

    public List<T> getTsInOrderOfValueDescending() {
        return tree.descendingMap().values().stream().flatMap(Set::stream).collect(Collectors.toList()); // is ascending order ?!
    }

    /** Adds the {@link T} @param t to the Tree Maintainer. */
    public void add(T t) {
        if (!set.contains(t)) {
            double submission = function.apply(t);
            boolean setok = set.add(t);
            tree.computeIfAbsent(submission, s -> new HashSet<>()) //
                    /* tree.get(submission) */ .add(t);
            GlobalAssert.that(setok);
        }
    }

    /** Removes the {@link T} @param t from the Tree Maintainer. */
    public void remove(T t) {
        if (contains(t)) {
            double value = function.apply(t);
            boolean setok = set.remove(t);
            boolean treeok = tree.get(value).remove(t);
            GlobalAssert.that(setok && treeok);
            if (tree.get(value).isEmpty())
                tree.remove(value);
        }
    }

    public void removeAllElementsWithValueSmaller(double minValue) {
        if (!tree.isEmpty()) {
            // TODO @clruch make use of already sorted Navigable Map
            Set<T> toRemoveSet = getTsInOrderOfValue().stream()//
                    .filter(t -> function.apply(t) <= minValue).collect(Collectors.toSet());
            toRemoveSet.forEach(this::remove);

            // getTsInOrderOfValue().stream().filter(t -> function.apply(t) <= minValue).distinct().forEachOrdered(this::remove);
        }
    }

    public Set<T> getValues() {
        return new HashSet<>(set);
    }

    public boolean contains(T t) {
        return set.contains(t);
    }

    public int size() {
        int numberTreeElements = tree.values().stream().mapToInt(Set::size).sum();
        GlobalAssert.that(numberTreeElements == set.size());
        return tree.size();
    }
}
