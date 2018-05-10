// code by Samuel Stauber
package ch.ethz.idsc.amodeus.util.hungarian;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Nodes {
    private final Set<Integer> nodes = new HashSet<>();
    private final Set<Integer> notNodes;

    public Nodes(int dim) {
        notNodes = IntStream.range(0, dim).boxed().collect(Collectors.toSet());
    }

    public void add(int i) {
        notNodes.remove(i);
        nodes.add(i);
    }

    public Set<Integer> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<Integer> getNotNodes() {
        return notNodes;
    }

    public void clear() {
        notNodes.addAll(nodes);
        nodes.clear();
    }
}
