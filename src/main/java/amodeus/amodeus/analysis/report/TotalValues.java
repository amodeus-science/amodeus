/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.report;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.util.io.FileLines;

/**
 * This class can be used to specify and export TotalValues properties.
 */
public class TotalValues implements AnalysisReport {
    public static final String DEFAULT_FILENAME = "totalSimulationValues.properties";
    // ---
    private final Properties properties = new Properties();
    private final Set<TotalValueAppender> totalValueAppenders = new HashSet<>();
    private final File dataDirectory;

    /**
     * Constructor
     *
     * @param dataDirectory File to which the total value properties are to be exported to
     */
    public TotalValues(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Appends a new TotalValueAppender. The total value propoerties represented by {@TotalValueAppender totalValueAppender}
     * will be written to the total value properties file when it is generated.
     *
     * @param totalValueAppender TotalValueAppender representing total value properties that are to be appended to this total
     *                           value object.
     */
    public void append(TotalValueAppender totalValueAppender) {
        totalValueAppenders.add(totalValueAppender);
    }

    /**
     * @param values Map<TotalValueIdentifier, String> contains total value identifiers and
     *               their corresponding value as string. Use this function to add these
     *               id,value pairs to this total value properties object
     */
    private void add(Map<TotalValueIdentifier, String> values) {
        values.forEach((key, value) -> properties.setProperty(key.getIdentifier(), value));
    }

    /**
     * Export total value properties of this TotalValueObject to a file.
     *
     * @param analysisSummary - unused.
     */
    @Override public void generate(AnalysisSummary analysisSummary) {
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

    /**
     * Load properties total simulation values properties from the object's current data directory
     *
     * @return {@Properties properties} total simulation values properties loaded from {@this.dataDirectory}
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Properties loadProperties() throws FileNotFoundException, IOException {
        return loadProperties(dataDirectory);
    }

    /**
     * Load properties total simulation values properties from a specific data directory
     *
     * @param dataDirectory File to directory from which the total simulation value properties should be loaded from
     * @return {@Properties properties} total simulation values properties loaded from {@File dataDirectory}
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Properties loadProperties(File dataDirectory) throws FileNotFoundException, IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(dataDirectory, DEFAULT_FILENAME);
        if (propertiesFile.isFile())
            properties.load(new FileInputStream(propertiesFile));
        return properties;
    }
}
