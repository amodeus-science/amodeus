/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.dataclean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public abstract class AbstractDataCleaner<T> {
    private final List<DataFilter<T>> filters = new ArrayList<>();

    public final void addFilter(DataFilter<T> filter) {
        filters.add(filter);
    }

    public final File clean(File file, ScenarioOptions simOptions, Network network) throws IOException {
        GlobalAssert.that(file.exists());
        System.out.println("Start to clean " + file.getAbsolutePath() + " data.");
        Stream<T> stream = readFile(file);
        for (DataFilter<T> dataFilter : filters) {
            System.out.println("Applying " + dataFilter.getClass().getSimpleName() + " on data.");
            stream = dataFilter.filter(stream, simOptions, network);
        }
        File outFile = writeFile(file, stream);
        System.out.println("Finished data cleanup.\n\tstored in " + outFile.getAbsolutePath());
        return outFile;
    }

    public abstract Stream<T> readFile(File file) throws IOException;

    public abstract File writeFile(File inFile, Stream<T> stream) throws IOException;

}
