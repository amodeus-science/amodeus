/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.cycling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

public class CyclePreventer {
    private Map<RoboTaxi, List<Link>> pastLinks = new HashMap<>();
    private final int maxCycle = 500;
    private final Map<RoboTaxi, Link> cyclingTaxis = new HashMap<>();

    /** Prevents cycling behavior in the set of @param allTaxis (supplied with
     * {@link amodeus.amodeus.dispatcher.core.BasicUniversalDispatcher#getRoboTaxis()}
     * by applying an cycling detection algorithm to the sequence of pst links. If cycling is detected, the {@link RoboTaxi}
     * is diverted towards its current drive destination to break the cycle with a @param rebalance command. */
    public void update(List<RoboTaxi> allTaxis, BiConsumer<RoboTaxi, Link> rebalance) {
        if (allTaxis.size() > 0) {
            /** maintaining a list of past links to prevent cycling */
            // if (pastLinks.size() == 0) {
            // allTaxis.stream().forEach(rt -> pastLinks.put(rt, new ArrayList<>()));
            // } else {
            // allTaxis.stream().forEach(rt -> pastLinks.get(rt).add(rt.getDivertableLocation()));
            // }
            allTaxis.forEach(rt -> pastLinks.computeIfAbsent(rt, r -> new ArrayList<>()).add(rt.getDivertableLocation())); // also adds first location

            /** there is only interest in repeated paths but not exact sequences of how these paths
             * are traveled on during time. */
            pastLinks.values().forEach(StaticHelper::removeDuplicates);

            /** check of roboTaxis are cycling */
            pastLinks.forEach((rt, links) -> {
                boolean isCycling = TortoiseAndHare.apply(links).hasNonTrivialCycle();
                if (isCycling) {
                    cyclingTaxis.put(rt, rt.getCurrentDriveDestination());
                    // links.stream().map(Link::getId).map(Id::toString).forEach(System.out::println);
                    // System.out.println("+++");
                }
                if (links.size() > maxCycle)
                    links.clear();
                // if (!isCycling && StaticHelper.containsMultiples(links)) {
                // links.stream().map(Link::getId).map(Id::toString).forEach(System.out::println);
                // System.out.println("===");
                // }
            });

            /** ensure cycling taxis reach their current drivedestination */
            Collection<RoboTaxi> cyclingPreventionCompleted = new ArrayList<>();
            cyclingTaxis.forEach((rt, l) -> {
                if (rt.getDivertableLocation().equals(l))
                    cyclingPreventionCompleted.add(rt);
            });
            cyclingPreventionCompleted.forEach(cyclingTaxis::remove);

            allTaxis.stream().filter(cyclingTaxis::containsKey).filter(RoboTaxi::isDivertable).forEach(rt -> rebalance.accept(rt, cyclingTaxis.get(rt)));
        }
    }

    public void printStatus() {
        if (cyclingTaxis.size() > 0)
            System.err.println("currently " + cyclingTaxis.size() + " taxis in cycling prevention.");
    }
}
