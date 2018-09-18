/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.lp.LPCreator;

public class LPOptions {

    protected final Properties properties;

    protected LPOptions(Properties properties) {
        this.properties = properties;
    }

    public LPOptions(File workingDirectory, Properties fallbackDefault) throws IOException {
        this.properties = StaticHelper.loadOrCreate(workingDirectory, fallbackDefault);
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

    public File getShapeFile() {
        File shapeFile = new File(getString(ScenarioOptionsBase.SHAPEFILEIDENTIFIER));
        System.out.println("shapeFile = " + shapeFile.getAbsolutePath());
        return shapeFile.exists() ? shapeFile : null;
    }

    // base access functions ==================================================
    public final String getString(String key) {
        return properties.getProperty(key);
    }

    public final boolean getBoolean(String key) {
        return Boolean.valueOf(properties.getProperty(key));
    }

    public final int getInt(String key) {
        return Integer.valueOf(properties.getProperty(key));
    }

    public final double getDouble(String key) {
        return Double.valueOf(properties.getProperty(key));
    }
}
