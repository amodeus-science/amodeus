/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityGenerator;
import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityGenerators;
import ch.ethz.idsc.amodeus.prep.PopulationCutter;
import ch.ethz.idsc.amodeus.prep.PopulationCutters;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public class ScenarioOptions {
    private final File workingDirectory;
    protected final Properties properties;

    @Deprecated
    /** Should not be used in amodeus repository anymore. */
    protected ScenarioOptions(Properties properties) {
        try {
            this.workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
            this.properties = properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ScenarioOptions(File workingDirectory, Properties fallbackDefault) throws IOException {
        this.workingDirectory = workingDirectory;
        this.properties = StaticHelper.loadOrCreateScenarioOptions(workingDirectory, fallbackDefault);
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    // PROPERTIES FUNCTIONS

    public final void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void saveAndOverwriteAmodeusOptions() throws IOException {
        ScenarioOptionsBase.saveProperties(workingDirectory, properties);
    }

    public void saveToFolder(File folder, String header) throws IOException {
        File file = new File(folder, ScenarioOptionsBase.getOptionsFileName());
        ScenarioOptionsBase.saveProperties(properties, file, header);
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

    public String getColorScheme() {
        return getString(ScenarioOptionsBase.COLORSCHEMEIDENTIFIER);
    }

    public String getChartTheme() {
        return getString(ScenarioOptionsBase.CHARTTHEMEIDENTIFIER);
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

    /** Hint: upcase instance of LocationSpec if necessary
     * 
     * @return */
    public final LocationSpec getLocationSpec() {
        return LocationSpecDatabase.INSTANCE.fromString( //
                properties.getProperty(ScenarioOptionsBase.LOCATIONSPECIDENTIFIER));
    }

    public PopulationCutter getPopulationCutter() {
        return PopulationCutters.valueOf(getString(ScenarioOptionsBase.POPULATIONCUTTERIDENTIFIER));
    }

    public VirtualNetworkCreator getVirtualNetworkCreator() {
        return VirtualNetworkCreators.valueOf(getString(ScenarioOptionsBase.VIRTUALNETWORKCREATORIDENTIFIER));
    }

    public int getMaxPopulationSize() {
        return getInt(ScenarioOptionsBase.MAXPOPULATIONSIZEIDENTIFIER);
    }

    public void setMaxPopulationSize(int maxNumberPeople) {
        properties.setProperty(ScenarioOptionsBase.MAXPOPULATIONSIZEIDENTIFIER, String.valueOf(maxNumberPeople));
    }

    public AVSpatialCapacityGenerator getParkingCapacityGenerator() {
        return AVSpatialCapacityGenerators.valueOf(getString(ScenarioOptionsBase.PARKINGGENERATORIDENTIFIER)).setScenarioOptions(this);
    }

    public String getParkingSpaceTagInNetwork() {
        return getString(ScenarioOptionsBase.PARKINGSPOTSTAGIDENTIFIER);
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
