/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LinkModes {

    public static final String ALLMODES = "all";
    // ---
    private final Set<String> modes;
    public final boolean allModesAllowed;

    public LinkModes(String spaceSeperatedString) {
        modes = new HashSet<>(Arrays.asList(spaceSeperatedString.split("\\s")));
        allModesAllowed = modes.contains(ALLMODES);
    }

    public Set<String> getModesSet() {
        return modes;
    }

}
