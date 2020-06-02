/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.io;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Util class to retrieve a set of files from a folder, usage examples:
 * // all files
 * List<File> files = new MultiFileReader(directory).getFolderFiles();
 * // all files containing nameContainString
 * List<File> files2 = new MultiFileReader(directory,"nameContainString").getFolderFiles();
 * // all files containing nameContainString with extension .ext
 * List<File> files3 = new MultiFileReader(directory,"nameContainString","ext").getFolderFiles(); **/
public class MultiFileReader {

    private final File filesDirectory;
    private final List<File> trailFiles;

    public MultiFileReader(File filesDirectory, String sharedFileName) {
        this.filesDirectory = filesDirectory;
        trailFiles = getAllFiles(sharedFileName);
    }

    public MultiFileReader(File filesDirectory) {
        this.filesDirectory = filesDirectory;
        trailFiles = getAllFiles();
    }

    public MultiFileReader(File filesDirectory, String sharedFileName, String extension) {
        this.filesDirectory = filesDirectory;
        trailFiles = getAllFiles(sharedFileName, extension);
    }

    public List<File> getFolderFiles() {
        return Collections.unmodifiableList(trailFiles);
    }

    /** @return all files in filesDirectory */
    private List<File> getAllFiles() {
        return filesDirectory.isDirectory() //
                ? Optional.ofNullable(filesDirectory.listFiles()).map(Arrays::asList).orElseGet(Collections::emptyList) //
                : Collections.emptyList();
    }

    /** @param sharedFileName
     * @return all files in filesDirectory that have the sequence @param
     *         sharedFileName in their filename */
    private List<File> getAllFiles(String sharedFileName) {
        if (filesDirectory.isDirectory())
            return Optional.ofNullable(filesDirectory.listFiles()).map(Arrays::stream).orElseGet(Stream::empty) //
                    .filter(file -> file.getName().contains(sharedFileName)).collect(Collectors.toList());
        return Collections.emptyList();

    }

    /** @param sharedFileName
     * @param extension
     * @return all files in filesDirectory that have the sequence @param
     *         sharedFileName in their filename and have @param extension */
    private List<File> getAllFiles(String sharedFileName, String extension) {
        return getAllFiles(sharedFileName).stream().filter(file -> new Filename(file).hasExtension(extension)).collect(Collectors.toList());
    }
}
