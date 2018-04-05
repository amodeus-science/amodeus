/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecs;
import ch.ethz.idsc.amodeus.prep.PopulationCutters;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;

public class ScenarioOptions {

    private final Properties properties;

    public static ScenarioOptions load(File workingDirectory) throws IOException {
        Properties properties = ScenarioOptionsBase.load(workingDirectory);
        return new ScenarioOptions(properties);
    }

    private ScenarioOptions(Properties properties) {
        this.properties = properties;
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    // specific access functions ==============================================
    public String getSimulationConfigName() {
        return getString(ScenarioOptionsBase.SIMUCONFIGIDENTIFIER);
    }

    public String getPreparerConfigName() {
        return getString(ScenarioOptionsBase.FULLCONFIGIDENTIFIER);
    }

    public String getVirtualNetworkName() {
        return getString(ScenarioOptionsBase.VIRTUALNETWORKNAMEIDENTIFIER);
    }

    public int getNumVirtualNodes() {
        return getInt(ScenarioOptionsBase.NUMVNODESIDENTIFIER);
    }

    public boolean isCompleteGraph() {
        return getBoolean(ScenarioOptionsBase.COMPLETEGRAPHIDENTIFIER);
    }

    public String getTravelDataName() {
        return getString(ScenarioOptionsBase.TRAVELDATAFILENAME);
    }

    public String getLinkSpeedDataName() {
        return getString(ScenarioOptionsBase.LINKSPEEDDATAFILENAME);
    }

    public LocationSpec getLocationSpec() {
        return LocationSpecs.DATABASE.fromString( //
                properties.getProperty(ScenarioOptionsBase.LOCATIONSPECIDENTIFIER));
    }

    public int getdtTravelData() {
        return getInt(ScenarioOptionsBase.DTTRAVELDATAIDENTIFIER);
    }

    public String getPreparedNetworkName() {
        return getString(ScenarioOptionsBase.NETWORKUPDATEDNAMEIDENTIFIER);
    }

    public String getPreparedPopulationName() {
        return getString(ScenarioOptionsBase.POPULATIONUPDATEDNAMEIDENTIFIER);
    }

    public PopulationCutters getPopulationCutter() {
        return PopulationCutters.valueOf(getString(ScenarioOptionsBase.POPULATIONCUTTERIDENTIFIER));
    }

    public VirtualNetworkCreators getVirtualNetworkCreator() {
        return VirtualNetworkCreators.valueOf(getString(ScenarioOptionsBase.VIRTUALNETWORKCREATORIDENTIFIER));
    }

    public int getMaxPopulationSize() {
        return getInt(ScenarioOptionsBase.MAXPOPULATIONSIZEIDENTIFIER);
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

}
