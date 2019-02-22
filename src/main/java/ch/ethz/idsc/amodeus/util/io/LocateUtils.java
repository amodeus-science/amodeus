/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum LocateUtils {
    ;

    public static File getWorkingDirectory() {
        try {
            return new File("").getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static File getSuperFolder(String name) {
        File file = new File(LocateUtils.class.getResource("LocateUtils.class").getFile());
        System.out.println("1: " + file.getAbsolutePath());
        
        System.out.println("file: " + file.getAbsolutePath());
        GlobalAssert.that(file.getAbsolutePath().contains(name));
        boolean test = true;
        while (!file.getName().endsWith(name) || test) {
            System.out.println("while-loop " +  file.getAbsolutePath());
            if (file.getName().endsWith("amodeus"))
                test = false;
            file = file.getParentFile();
        }
        System.out.println("return: " + file.getAbsolutePath());
        return file;
    }

}
