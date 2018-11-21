package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TreeMultipleItems<T> {

    private final NavigableMap<Double, Set<T>> tree = new TreeMap<>();
    /** data structures are used to enable fast "contains" searching */
    private final Set<T> set = new HashSet<>();
    private final Function<T, Double> time;

    public TreeMultipleItems(Function<T, Double> time) {
        this.time = time;
    }

    /** @return closest {@link T} in tree from {@link Coord} @param coord */
    public Set<T> getFirst() {
        return tree.firstEntry().getValue();
    }

    public List<T> getTsInOrderOfValue() {
        List<T> list = new ArrayList<>();
        tree.values().forEach(ts -> list.addAll(ts)); // is asscending order ?!
        return list;
    }
    
    public List<T> getTsInOrderOfValueDescending() {
        List<T> list = new ArrayList<>();
        tree.descendingMap().values().forEach(ts -> list.addAll(ts)); // is asscending order ?!
        return list;
    }

    /** Adds the {@link T} @param t to the Tree Maintainer. */
    public void add(T t) {
        if (!set.contains(t)) {
            Double submission = time.apply(t);
            boolean setok = set.add(t);
            if (tree.containsKey(submission)) {
                tree.get(submission).add(t);
            } else {
                Set<T> newSet = new HashSet<>();
                newSet.add(t);
                tree.put(submission, newSet);
            }
            // TODO add check for tree
            GlobalAssert.that(setok);
        }
    }

    /** Removes the {@link T} @param t from the Tree Maintainer. */
    public void remove(T t) {
        if (contains(t)) {
            Double submission = time.apply(t);
            boolean setok = set.remove(t);
            boolean treeok = tree.get(submission).remove(t);
            GlobalAssert.that(setok && treeok);
            if (tree.get(submission).isEmpty()) {
                tree.remove(submission);
            }
        }

    }

    public void removeAllElementsWithValueSmaller(Double minValue) {
        if (!tree.isEmpty()) {
            // TODO make use of allready sorted Navigable Map
            Set<T> toRemoveSet = getTsInOrderOfValue().stream().filter(t-> time.apply(t) <= minValue).collect(Collectors.toSet());
            toRemoveSet.forEach(t-> remove(t));
        }

    }

    public Set<T> getValues() {
        return new HashSet<>(set);
    }

    public boolean contains(T t) {
        return set.contains(t);
    }

    public int size() {
        int numberTreeElements = tree.values().stream().mapToInt(s -> s.size()).sum();
        GlobalAssert.that(numberTreeElements == set.size());
        return tree.size();
    }
}
