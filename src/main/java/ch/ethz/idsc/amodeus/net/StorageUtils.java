/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class StorageUtils {

    /** the output folder is created by MATSim */
    private static final File DEFAULT_OUTPUT_DIRECTORY = new File("output");
    private static final String SIMOBJ = "simobj";
    // ---
    private final File output;
    private final File directory;

    public StorageUtils(File outputdirectory) {
        System.out.println("StorageUtils output directory location: " + outputdirectory.getAbsolutePath());
        if (outputdirectory.isDirectory()) {
            output = outputdirectory;
        } else {
            System.err.println(outputdirectory.getAbsolutePath());
            System.out.println("supplied outputdircetory does not exist, using default");
            System.out.println("outputdirectory = " + DEFAULT_OUTPUT_DIRECTORY.getAbsolutePath());
            output = DEFAULT_OUTPUT_DIRECTORY;
        }
        directory = new File(output, SIMOBJ);
    }

    public void printStorageProperties() {
        System.out.println("StorageUtils object has properties:");
        System.out.println("OUTPUT File: " + output.getAbsolutePath());
        System.out.println("DIRECTORY File: " + directory.getAbsolutePath());
        System.out.println(output.isDirectory() ? "OUTPUT is present" : "OUTPUT not present");
        System.out.println(directory.isDirectory() ? "DIRECTORY is present" : "DIRECTORY not present");
    }

    /** @return {@link List} of {@link IterationFolder} where simulation results
     *         for visualization are stored. */
    public List<IterationFolder> getAvailableIterations() {
        if (!directory.isDirectory()) {
            System.out.println("no iterations found");
            return Collections.emptyList();
        }

        List<IterationFolder> returnList = new ArrayList<>();
        Stream.of(directory.listFiles()).sorted() //
                .forEach(f -> returnList.add(new IterationFolder(f, this)));
        return returnList;
    }

    /** @return {@link NavigableMap} to with {@link Integer} and {@link File} for
     *         the first available iteration. */
    public NavigableMap<Integer, File> getFirstAvailableIteration() {
        if (!directory.isDirectory()) { // no simobj directory
            System.out.println("no files found");
            return Collections.emptyNavigableMap();
        }

        File[] files = Stream.of(directory.listFiles()).sorted().toArray(File[]::new);

        if (files.length == 0) {
            System.out.println("no files found");
            return Collections.emptyNavigableMap();
        }

        File lastIter = files[files.length - 1];
        System.out.println("loading last Iter = " + lastIter);
        return getFrom(lastIter);
    }

    /** function only called from {@link StorageSubscriber} when data is recorded
     * during simulation
     * 
     * @param simulationObject
     * 
     * @return file to store given simulationObject */
    /* package */ File getFileForStorageOf(SimulationObject simulationObject) {
        GlobalAssert.that(output.exists());

        directory.mkdir();
        File iter = new File(directory, String.format("it.%02d", simulationObject.iteration));
        iter.mkdir();
        long floor = (simulationObject.now / 1000) * 1000;
        File folder = new File(iter, String.format("%07d", floor));
        folder.mkdir();
        GlobalAssert.that(folder.isDirectory());
        return new File(folder, String.format("%07d.bin", simulationObject.now));
    }

    /** @param itDir
     *            {@link File} with iteration folder
     * @return {@link NavigableMap} with time as {@link Integer} and
     *         {@link File} with iteration result. */
    /* package */ NavigableMap<Integer, File> getFrom(File itDir) {
        NavigableMap<Integer, File> navigableMap = new TreeMap<>();
        for (File dir : itDir.listFiles())
            if (dir.isDirectory())
                for (File file : dir.listFiles())
                    if (file.isFile())
                        navigableMap.put(Integer.parseInt(file.getName().substring(0, 7)), file);
        return navigableMap;
    }
}
