/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;

public enum ColorDataAmodeus {
    INSTANCE;
    // ---
    public static ColorDataIndexed indexed(String name) {
        ColorDataIndexed colorDataIndexed = INSTANCE.map.get(normalize(name));
        return Objects.isNull(colorDataIndexed) //
                ? ColorDataAmodeusSpecific.COLORFUL.cyclic()
                : colorDataIndexed;
    }

    // ---
    private final Map<String, ColorDataIndexed> map = new HashMap<>();

    private ColorDataAmodeus() {
        for (ColorDataLists colorDataLists : ColorDataLists.values())
            put(colorDataLists.name(), colorDataLists.cyclic());

        for (ColorDataAmodeusSpecific colorDataAmodeusSpecific : ColorDataAmodeusSpecific.values())
            put(colorDataAmodeusSpecific.name(), colorDataAmodeusSpecific.cyclic());
    }

    private void put(String name, ColorDataIndexed colorDataIndexed) {
        map.put(normalize(name), colorDataIndexed);
    }

    private static String normalize(String name) {
        return name.charAt(0) == '_' //
                ? normalize(name.substring(1))
                : name.toUpperCase();
    }
}
