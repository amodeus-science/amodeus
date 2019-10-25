/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import junit.framework.TestCase;

public class ColorDataAmodeusSpecificTest extends TestCase {
    public void testSimple() {
        for (ColorDataAmodeusSpecific cdas : ColorDataAmodeusSpecific.values()) {
            assertTrue(IntStream.range(0, cdas.size()).mapToObj(cdas.cyclic()::getColor).allMatch(Objects::nonNull));
            assertEquals(cdas.size(), //
                    IntStream.range(0, 2 * cdas.size()).mapToObj(cdas.cyclic()::getColor).collect(Collectors.toSet()).size());
        }
    }
}
