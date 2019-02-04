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

public class ScenarioOptions {

    protected final Properties properties;

    protected ScenarioOptions(Properties properties) {
        this.properties = properties;
    }

    public ScenarioOptions(File workingDirectory, Properties fallbackDefault) throws IOException {
        this.properties = StaticHelper.loadOrCreateScenarioOptions(workingDirectory, fallbackDefault);
    }

    // PROPERTIES FUNCTIONS

    public final void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public void saveAndOverwriteAmodeusOptions() throws IOException {
        ScenarioOptionsBase.saveProperties(properties);
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
    
    public int getMaxNumberRequests() {
        return getInt(ScenarioOptionsBase.MAXNUMBERREQUESTSIDENTIFIER);
    }

    public void setMaxNumberRequests(int maxNumberRequests) {
        properties.setProperty(ScenarioOptionsBase.MAXNUMBERREQUESTSIDENTIFIER, String.valueOf(maxNumberRequests));
    }
    
    public int getMinimumTripDistance() {
        return getInt(ScenarioOptionsBase.MINIMUMTRIPDISTANCE);
    }

    public void setMinimumTripDistance(int minTripDistance) {
        properties.setProperty(ScenarioOptionsBase.MINIMUMTRIPDISTANCE, String.valueOf(minTripDistance));
    }
    
    public double getModifierLinkFreeSpeed() {
        return getDouble(ScenarioOptionsBase.MODFYLINKFREESPEEDIDENTIFIER);
    }

    public void setModifierLinkFreeSpeed(double modifier) {
        properties.setProperty(ScenarioOptionsBase.MODFYLINKFREESPEEDIDENTIFIER, String.valueOf(modifier));
    }
    
    public int getMPCplanningHorizon() {
        return getInt(ScenarioOptionsBase.MPCPLANNINGHORIZONIDENTIFIER);
    }

    public void setMPCplanningHorizon(int planningHorizon) {
        properties.setProperty(ScenarioOptionsBase.MPCPLANNINGHORIZONIDENTIFIER, String.valueOf(planningHorizon));
    }
    
    public int getMPCtimeStep() {
        return getInt(ScenarioOptionsBase.MPCTIMESTEPIDENTIFIER);
    }

    public void setMPCtimeStep(int timeStep) {
        properties.setProperty(ScenarioOptionsBase.MPCTIMESTEPIDENTIFIER, String.valueOf(timeStep));
    }
    
    public double getMPCRebalancePunisher() {
        return getDouble(ScenarioOptionsBase.MPCREBALANCEPUNISHER);
    }

    public void setMPCRebalancePunisher(double rebalancePunisher) {
        properties.setProperty(ScenarioOptionsBase.MPCREBALANCEPUNISHER, String.valueOf(rebalancePunisher));
    }
    
    public boolean isMPCmilp() {
        return getBoolean(ScenarioOptionsBase.MPCMILPFLAGIDENTIFIER);
    }

    public void setMPCmilp(boolean milpFlag) {
        properties.setProperty(ScenarioOptionsBase.MPCMILPFLAGIDENTIFIER, String.valueOf(milpFlag));
    }
    
    public boolean allowAssistance() {
        return getBoolean(ScenarioOptionsBase.MPCASSISTANCEIDENTIFIER);
    }

    public void setAssistance(boolean assistanceFlag) {
        properties.setProperty(ScenarioOptionsBase.MPCASSISTANCEIDENTIFIER, String.valueOf(assistanceFlag));
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
    
    public int getNumberVehicles() {
        return getInt(ScenarioOptionsBase.NUMBEROFVEHICLESIDENTIFIER);
    }

    public void setNumberVehicles(int numberVehicles) {
        properties.setProperty(ScenarioOptionsBase.NUMBEROFVEHICLESIDENTIFIER, String.valueOf(numberVehicles));
    }
    
    public int getNetworkRadiusCut() {
        return getInt(ScenarioOptionsBase.NETWORKRADIUSCUTTER);
    }

    public void setNetworkRadiusCut(int radius) {
        properties.setProperty(ScenarioOptionsBase.NETWORKRADIUSCUTTER, String.valueOf(radius));
    }
    
    public String getDispatcher() {
        return getString(ScenarioOptionsBase.DISPATCHERSTRATEGYIDENTIFIER);
    }

    public void setDispatcher(String dispatcher) {
        properties.setProperty(ScenarioOptionsBase.DISPATCHERSTRATEGYIDENTIFIER, dispatcher);
    }
    
    public boolean getMakeVideo() {
        return getBoolean(ScenarioOptionsBase.MAKEVIDEOIDENTIFIER);
    }

    public void setMakeVideo(boolean makeVideo) {
        properties.setProperty(ScenarioOptionsBase.DISPATCHERSTRATEGYIDENTIFIER, String.valueOf(makeVideo));
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
