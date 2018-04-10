/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum MultiFileTools {
    ;

    /** @return {@link File} of current working directory
     * @throws IOException */
    public static File getWorkingDirectory() throws IOException {
        return new File("").getCanonicalFile();
    }

    /** @return all directories in filesDirectory sorted by name */
    public static File[] getAllDirectoriesSorted(File filesDirectory) {
        GlobalAssert.that(filesDirectory.isDirectory());
        return Stream.of(filesDirectory.listFiles())//
                .filter(File::isDirectory)//
                .sorted().toArray(File[]::new);
    }

}
