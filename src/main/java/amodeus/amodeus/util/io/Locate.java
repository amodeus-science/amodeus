/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.io;

import java.io.File;

import amodeus.amodeus.util.math.GlobalAssert;

public enum Locate {
    ;

    /** @return top folder of repository with name @param repoName in which
     *         some {@link Class} @param someClass is located. Usage sample:
     * 
     *         File directory = new File(LocateUtils.getSuperFolder(CurrentClass.class, "amodeus"), "resources/testScenario"); */
    public static File repoFolder(Class<?> someClass, String repoName) {
        File file = new File(someClass.getResource(someClass.getSimpleName() + ".class").getFile());
        GlobalAssert.that(file.getAbsolutePath().contains(repoName));
        boolean test = true;
        boolean amodeus = repoName.equals("amodeus");
        while (!file.getName().endsWith(repoName) || test || amodeus) {
            if (file.getName().endsWith(repoName))
                if (amodeus)
                    amodeus = false;
                else
                    test = false;
            file = file.getParentFile();
        }
        return file;
    }
}
