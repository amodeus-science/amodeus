/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.plot;

/* package */ enum StaticHelperColor {
    ;
    static String colorlist(String name) {
        return "/colorlist/" + name.toLowerCase() + ".csv";
    }
}
