/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum MultiFileTools {
    ;

    /** @return default working directory as set in the Runtime configurations, please use this
     *         function only in main functions
     * 
     * @throws IOException */
    public static File getDefaultWorkingDirectory() {
        try {
            return new File(".").getCanonicalFile();
        } catch (Exception e) {
            System.err.println("Cannot load working directory, returning null: ");
            e.printStackTrace();
            return null;
        }
    }

    /** @return all directories in filesDirectory sorted by name */
    public static File[] getAllDirectoriesSorted(File filesDirectory) {
        return streamAllDirectories(filesDirectory).sorted().toArray(File[]::new);
    }

    /** @return all directories in filesDirectory sorted by name with a subfolder of name folderName */
    public static File[] getAllDirectoriesSortedWithSubfolderName(File filesDirectory, String folderName) {
        return streamAllDirectories(filesDirectory) //
                .filter(v -> containsSubfolderName(v, folderName)).sorted().toArray(File[]::new);
    }

    /** @return true if the filesDirectory contains any subfolder (including itself) with the name folderName */
    public static boolean containsSubfolderName(File filesDirectory, String folderName) {
        if (filesDirectory.getName().equals(folderName))
            return true;
        return streamAllDirectories(filesDirectory).anyMatch(v -> containsSubfolderName(v, folderName));
    }

    /** @return true if the filesDirectory contains a folder with the name folderName */
    public static boolean containsFolderName(File filesDirectory, String folderName) {
        return streamAllDirectories(filesDirectory).anyMatch(v -> v.getName().equals(folderName));
    }

    /** @return stream of all directories in filesDirectory */
    private static Stream<File> streamAllDirectories(File filesDirectory) {
        GlobalAssert.that(filesDirectory.isDirectory());
        return Optional.ofNullable(filesDirectory.listFiles()).map(Stream::of).orElseGet(Stream::empty) //
                .filter(File::isDirectory);
    }
}
