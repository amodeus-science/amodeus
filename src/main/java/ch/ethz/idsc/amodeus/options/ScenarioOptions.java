/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import ch.ethz.idsc.amodeus.parking.ParkingCapacityGenerator;
import ch.ethz.idsc.amodeus.parking.ParkingCapacityGenerators;
import ch.ethz.idsc.amodeus.parking.strategies.ParkingStrategies;
import ch.ethz.idsc.amodeus.parking.strategies.ParkingStrategy;
import ch.ethz.idsc.amodeus.prep.PopulationCutter;
import ch.ethz.idsc.amodeus.prep.PopulationCutters;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreator;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;

public class ScenarioOptions {
    private final File workingDirectory;
    protected final Properties properties;

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

    public void saveAndOverwriteAmodeusOptions() {
        ScenarioOptionsBase.savePropertiesToDirectory(workingDirectory, properties);
    }

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
        return ParkingCapacityGenerators.valueOf(getString(ScenarioOptionsBase.PARKINGGENERATORIDENTIFIER)).setScenarioOptions(this);
    }

    public String getParkingSpaceTagInNetwork() {
        return getString(ScenarioOptionsBase.PARKINGSPOTSTAGIDENTIFIER);
    }

    public ParkingStrategy getParkingStrategy() {
        return ParkingStrategies.valueOf(getString(ScenarioOptionsBase.PARKINGSTRATEGYIDENTIFIER)).generateParkingStrategy();
    }

    public File getShapeFile() {
        File shapeFile = new File(workingDirectory, getString(ScenarioOptionsBase.SHAPEFILEIDENTIFIER));
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
