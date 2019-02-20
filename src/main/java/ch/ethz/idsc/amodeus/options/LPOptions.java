/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.lp.LPCreator;

public class LPOptions {
    private final File workingDirectory;
    protected final Properties properties;

    public LPOptions(File workingDirectory, Properties fallbackDefault) throws IOException {
        this.workingDirectory = workingDirectory;
        this.properties = StaticHelper.loadOrCreateLPOptions(workingDirectory, fallbackDefault);
    }

    // PROPERTIES FUNCTIONS

    public final void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void saveAndOverwriteLPOptions() throws IOException {
        LPOptionsBase.saveProperties(workingDirectory, properties);
    }

    public void saveToFolder(File folder, String header) throws IOException {
        File file = new File(folder, LPOptionsBase.getOptionsFileName());
        LPOptionsBase.saveProperties(properties, file, header);
    }

    public double getLPWeightQ() {
        return getDouble(ScenarioOptionsBase.LPWEIGHTQ);
    }

    public double getLPWeightR() {
        return getDouble(ScenarioOptionsBase.LPWEIGHTR);
    }

    public LPCreator getLPSolver() {
        return LPCreator.valueOf(getString(ScenarioOptionsBase.LPSOLVER).toUpperCase());
    }

    // base access functions ==================================================
    public final String getString(String key) {
        return properties.getProperty(key);
    }

    public final double getDouble(String key) {
        return Double.valueOf(properties.getProperty(key));
    }
}
