/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum Locate {
    ;

    /** @return top folder of repository with name @param repoName in which
     *         some {@link Class} @param someClass is located. Usage sample:
     * 
     *         File directory = new File(LocateUtils.getSuperFolder(CurrentClass.class,"amodeus"), "resources/testScenario"); */
    public static File repoFolder(Class<?> someClass, String repoName) {
        File file = new File(someClass.getResource(someClass.getSimpleName() + ".class").getFile());
        GlobalAssert.that(file.getAbsolutePath().contains(repoName));
        boolean test = true;
        while (!file.getName().endsWith(repoName) || test) {
            if (file.getName().endsWith(repoName))
                test = false;
            file = file.getParentFile();
        }
        return file;
    }
}
