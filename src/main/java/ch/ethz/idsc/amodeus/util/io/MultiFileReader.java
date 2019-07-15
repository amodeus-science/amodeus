/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /** @param sharedFileName
     * @return all files in filesDirectory that have the sequence @param
     *         sharedFileName in their filename */
    private List<File> getAllFiles() {
        List<File> relevantFiles = new ArrayList<>();
        if (filesDirectory.isDirectory())
            for (File file : filesDirectory.listFiles()) {
                relevantFiles.add(file);
            }
        return relevantFiles;
    }

    /** @param sharedFileName
     * @return all files in filesDirectory that have the sequence @param
     *         sharedFileName in their filename */
    private List<File> getAllFiles(String sharedFileName) {
        List<File> relevantFiles = new ArrayList<>();
        if (filesDirectory.isDirectory())
            for (File file : filesDirectory.listFiles()) {
                if (file.getName().contains(sharedFileName)) {
                    relevantFiles.add(file);
                }
            }
        return relevantFiles; // TODO DHR throw exception here or earlier

    }

    /** @param sharedFileName
     * @return all files in filesDirectory that have the sequence @param
     *         sharedFileName in their filename */
    private List<File> getAllFiles(String sharedFileName, String extension) {
        List<File> relevantFiles = getAllFiles(sharedFileName);
        List<File> relevantFilesExt = new ArrayList<>();
        for (File file : relevantFiles)
            if (new Filename(file).hasExtension(extension))
                relevantFilesExt.add(file);
        return relevantFilesExt;

    }

}
