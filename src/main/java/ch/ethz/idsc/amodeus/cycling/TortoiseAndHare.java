/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.cycling;

import java.util.List;
import java.util.Objects;

/** Implementation of the Tortoise and Hare algorithm to detect cyles in {@link List}s.
 * Reference, e.g., at
 *
 * https://en.wikipedia.org/wiki/Cycle_detection
 *
 * @author clruch
 *
 * @param <T> */
class TortoiseAndHare<T> {

    private final List<T> values;
    private Integer tortoise = null;
    private Integer hare = null;
    private Integer begin = null;
    private Integer lngth = null;
    private boolean hasRep = true;

    public static <T> TortoiseAndHare<T> apply(List<T> values) {
        return new TortoiseAndHare<>(values);
    }

    /** @return true if {@link this.values} contains nontrivial cycles
     *         with period > 1, e.g., {1,2,3,4,3,4,3,4,3,...} */
    public boolean hasNonTrivialCycle() {
        if (!hasRep)
            return false;
        if (Objects.isNull(begin))
            return false;
        if (Objects.isNull(lngth))
            return false;
        if (lngth == 1)
            return false;
        // System.err.println("get here?");
        int index1 = begin;
        int index2 = begin + lngth;
        // System.err.println("index1: " + index1);
        // System.err.println("index2: " + index2);
        // System.err.println("valsiz: " + values.size());
        boolean isCycling = true;
        /** to ensure it is a real cycle, at least one full length must be covered */
        if ((index2 + lngth) > (values.size() - 1)) {
            isCycling = false;
            return isCycling;
        }

        while (index2 < values.size()) {
            // System.err.println("ever get here?");
            if (!values.get(index1).equals(values.get(index2))) {
                isCycling = false;
                break;
            }
            ++index1;
            ++index2;
        }
        return isCycling;
    }

    private TortoiseAndHare(List<T> values) {
        this.values = values;
        if (values.size() > 2)
            findRep();
        else
            hasRep = false;
        // System.out.println("hasRep 1: " + hasRep);
        if (hasRep)
            findFirstRep();
        if (hasRep)
            findLength();
        // System.out.println("begin: " + begin);
        // System.out.println("lngth: " + lngth);
    }

    private void findRep() {
        tortoise = 0;
        hare = 1;
        while (!values.get(tortoise).equals(values.get(hare))) {
            // System.out.println("fr tortoise: " + tortoise);
            // System.out.println("fr hare: " + hare);
            tortoise += 1;
            hare += 2;
            if (hare > values.size() - 1) {
                hasRep = false;
                break;
            }
        }
    }

    private void findFirstRep() {
        begin = 0;
        tortoise = 0;
        hare++;
        if ((hare > (values.size() - 1))) {
            hasRep = false;
            return;
        }
        // System.out.println("hasRep 2: " + hasRep);
        // System.out.println("ffr tortoise: " + tortoise);
        // System.out.println("ffr hare: " + hare);
        while (!values.get(tortoise).equals(values.get(hare))) {
            // System.out.println("ffr begin: " + begin);
            // System.out.println("ffr tortoise: " + tortoise);
            // System.out.println("ffr hare: " + hare);
            tortoise += 1;
            hare += 1;
            begin += 1;
            if ((hare > (values.size() - 1))) {
                hasRep = false;
                break;
            }
        }
        // System.out.println("ffr begin: " + begin);
    }

    private void findLength() {
        lngth = 1;
        hare = tortoise + 1;
        while (!values.get(tortoise).equals(values.get(hare))) {
            // System.out.println("fl tortoise: " + tortoise);
            // System.out.println("fl hare: " + hare);
            hare++;
            lngth += 1;
            if (hare > values.size() - 1) {
                hasRep = false;
                break;
            }
        }
    }
}