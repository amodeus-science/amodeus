/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.matsim.core.config.Config;

import amodeus.amodeus.data.LocationSpec;
import amodeus.amodeus.data.LocationSpecDatabase;
import amodeus.amodeus.parking.ParkingCapacityGenerator;
import amodeus.amodeus.parking.ParkingCapacityGenerators;
import amodeus.amodeus.parking.strategies.ParkingStrategies;
import amodeus.amodeus.parking.strategies.ParkingStrategy;
import amodeus.amodeus.prep.PopulationCutter;
import amodeus.amodeus.prep.PopulationCutters;
import amodeus.amodeus.prep.VirtualNetworkCreator;
import amodeus.amodeus.prep.VirtualNetworkCreators;

/** This class contains the various scenario parameter options that
 * are required to setup a simulation scenario */
public class ScenarioOptions {
    private final File workingDirectory;
    protected final Properties properties;

    /** Specify a working directory and a set of scenarios to fall back to,
     * to use as default settings
     * 
     * @param workingDirectory
     * @param fallbackDefault
     * @throws IOException */
    public ScenarioOptions(File workingDirectory, Properties fallbackDefault) throws IOException {
        this.workingDirectory = workingDirectory;
        this.properties = StaticHelper.loadOrCreateScenarioOptions(workingDirectory, fallbackDefault);
    }

    /** Returns the scenario working directory
     * 
     * @return */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    // PROPERTIES FUNCTIONS

    public final void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /** Save the current scenario option object to the working directory. Overwrite it if it exsists. */
    public void saveAndOverwriteAmodeusOptions() {
        ScenarioOptionsBase.savePropertiesToDirectory(workingDirectory, properties);
    }

    /** Save the scenario properties to a folder
     * 
     * @param folder
     * @param header - header string stored with the scenario properties (can be used to comment/discriminate scenario
     *            options) */
    public void saveToFolder(File folder, String header) {
        File file = new File(folder, ScenarioOptionsBase.OPTIONSFILENAME);
        ScenarioOptionsBase.savePropertiesToFileWithHeader(properties, file, header);
    }

    // specific access functions ==============================================
    public String getOutputDirectory(Config config) {
        return new File(workingDirectory, config.controler().getOutputDirectory()).getAbsolutePath();
    }

    public String getSimulationConfigName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.SIMUCONFIGIDENTIFIER)).getAbsolutePath();
    }

    public String getPreparerConfigName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.FULLCONFIGIDENTIFIER)).getAbsolutePath();
    }

    public String getVirtualNetworkDirectoryName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.VIRTUALNETWORKNAMEIDENTIFIER)).getAbsolutePath();
    }

    public String getVirtualNetworkName() {
        return getString(ScenarioOptionsBase.VIRTUALNETWORKNAMEIDENTIFIER);
    }

    public String getTravelDataName() {
        return getString(ScenarioOptionsBase.TRAVELDATAFILENAME);
    }

    public String getLinkSpeedDataName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.LINKSPEEDDATAFILENAME)).getAbsolutePath();
    }

    public String getPreparedNetworkName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.NETWORKUPDATEDNAMEIDENTIFIER)).getAbsolutePath();
    }

    public String getPreparedPopulationName() {
        return new File(workingDirectory, getString(ScenarioOptionsBase.POPULATIONUPDATEDNAMEIDENTIFIER)).getAbsolutePath();
    }

    public int getNumVirtualNodes() {
        return getInt(ScenarioOptionsBase.NUMVNODESIDENTIFIER);
    }

    public boolean isCompleteGraph() {
        return getBoolean(ScenarioOptionsBase.COMPLETEGRAPHIDENTIFIER);
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

    public ParkingCapacityGenerator getParkingCapacityGenerator() {
        System.err.println("Parking capacity generator:");
        System.err.println(getString(ScenarioOptionsBase.PARKINGGENERATORIDENTIFIER));
        return ParkingCapacityGenerators.valueOf(getString(ScenarioOptionsBase.PARKINGGENERATORIDENTIFIER));
    }

    public String getParkingSpaceTagInNetwork() {
        return getString(ScenarioOptionsBase.PARKINGSPOTSTAGIDENTIFIER);
    }

    public ParkingStrategy getParkingStrategy(Random random) {
        return ParkingStrategies.valueOf(getString(ScenarioOptionsBase.PARKINGSTRATEGYIDENTIFIER))//
                .generateParkingStrategy(random);
    }

    public File getShapeFile() {
        File shapeFile = new File(workingDirectory, getString(ScenarioOptionsBase.SHAPEFILEIDENTIFIER));
        System.out.println("shapeFile = " + shapeFile.getAbsolutePath());
        return shapeFile.exists() ? shapeFile : null;
    }

    public long getRandomSeed() {
        return Long.parseLong(properties.getProperty(ScenarioOptionsBase.RANDOMSEED));
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
