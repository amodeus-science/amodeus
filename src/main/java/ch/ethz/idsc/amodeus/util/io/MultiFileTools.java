/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum MultiFileTools {
    ;

    /** @return default working directory as set in the Runtime configurations, please use this
     *         function only in main functions 
     * 
     * @throws IOException */
    public static File getDefaultWorkingDirectory() throws IOException {
        return new File(".").getCanonicalFile();
    }

    @Deprecated
    /** Should not be used in amodeus repository anymore! */
    public static File getWorkingDirectory() throws IOException {
        return new File(".").getCanonicalFile();
    }

    /** @return all directories in filesDirectory sorted by name */
    public static File[] getAllDirectoriesSorted(File filesDirectory) {
        GlobalAssert.that(filesDirectory.isDirectory());
        return Stream.of(filesDirectory.listFiles())//
                .filter(File::isDirectory)//
                .sorted().toArray(File[]::new);
    }

    /** @return all directories in filesDirectory sorted by name with a subfolder of name folderName */
    public static File[] getAllDirectoriesSortedWithSubfolderName(File filesDirectory, String folderName) {
        GlobalAssert.that(filesDirectory.isDirectory());
        return Stream.of(filesDirectory.listFiles())//
                .filter(File::isDirectory)//
                .filter(v -> containsSubfolderName(v, folderName))//
                .sorted().toArray(File[]::new);
    }

    /** @return true if the filesDirectory contains any subfolder (including itself) with the name folderName */
    public static boolean containsSubfolderName(File filesDirectory, String folderName) {
        if (filesDirectory.getName().equals(folderName)) {
            return true;
        }
        GlobalAssert.that(filesDirectory.isDirectory());
        return Stream.of(filesDirectory.listFiles()).filter(File::isDirectory).anyMatch(v -> containsSubfolderName(v, folderName));
    }

    /** @return true if the filesDirectory contains a folder with the name folderName */
    public static boolean containsFolderName(File filesDirectory, String folderName) {
        GlobalAssert.that(filesDirectory.isDirectory());
        return Stream.of(filesDirectory.listFiles()).filter(File::isDirectory).anyMatch(v -> v.getName().equals(folderName));
    }

}
