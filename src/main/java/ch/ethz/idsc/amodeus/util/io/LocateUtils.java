/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum LocateUtils {
    ;

    // TODO this function is always called with a first argument "amodeus", take this into
    // this function.
    public static File getSuperFolder(String name) {
        File file = new File(LocateUtils.class.getResource("LocateUtils.class").getFile());
        GlobalAssert.that(file.getAbsolutePath().contains(name));
        boolean test = true;
        while (!file.getName().endsWith(name) || test) {
            if (file.getName().endsWith("amodeus"))
                test = false;
            file = file.getParentFile();
        }
        return file;
    }

}
