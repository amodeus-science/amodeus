/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum TestUtils {
    ;

    public static File getSuperFolder(String name) {
        File file = new File(TestUtils.class.getResource("TestUtils.class").getFile());
        System.out.println("file: " + file.getAbsolutePath());
        GlobalAssert.that(file.getAbsolutePath().contains(name));
        boolean test = true;
        while (!file.getName().endsWith(name) || test) {
            if (file.getName().endsWith("test-classes"))
                test = false;
            file = file.getParentFile();
        }
        return file;
    }

}
