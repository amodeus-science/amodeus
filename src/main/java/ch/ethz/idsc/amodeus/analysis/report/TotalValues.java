/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.io.FileLines;

public class TotalValues implements AnalysisReport {
    public static final String DEFAULT_FILENAME = "totalSimulationValues.properties";
    // ---
    private final Properties properties = new Properties();
    private final Set<TotalValueAppender> totalValueAppenders = new HashSet<>();
    private final File dataDirectory;

    public TotalValues(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void append(TotalValueAppender totalValueAppender) {
        totalValueAppenders.add(totalValueAppender);
    }

    private void add(Map<TotalValueIdentifier, String> values) {
        for (Entry<TotalValueIdentifier, String> value : values.entrySet())
            properties.setProperty(value.getKey().getIdentifier(), value.getValue());
    }

    @Override
    public void generate(AnalysisSummary analysisSummary) {
        for (TotalValueAppender totalValueAppender : totalValueAppenders)
            add(totalValueAppender.getTotalValues());

        File defaultFile = new File(dataDirectory, DEFAULT_FILENAME);

        try (FileOutputStream ostream = new FileOutputStream(defaultFile)) {
            properties.store(ostream, "Total Values for this Simulation (last Iteration)");
            System.out.println("Saved Total Properties to " + DEFAULT_FILENAME);
            FileLines.sort(defaultFile);
        } catch (IOException e) {
            System.err.println("The save of the Total Properties failed");
            e.printStackTrace();
        }

    }

    public Properties loadProperties() throws FileNotFoundException, IOException {
        return loadProperties(dataDirectory);
    }

    public static Properties loadProperties(File dataDirectory) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(dataDirectory, DEFAULT_FILENAME);
        if (propertiesFile.exists())
            properties.load(new FileInputStream(propertiesFile));
        return properties;
    }

}
