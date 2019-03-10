/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.dispatcher.parking.ParkingCapacityGenerators;
import ch.ethz.idsc.amodeus.dispatcher.parking.strategies.ParkingStrategies;
import ch.ethz.idsc.amodeus.prep.PopulationCutters;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;
import ch.ethz.idsc.amodeus.util.io.FileLines;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public enum ScenarioOptionsBase {
    ;

    static final String OPTIONSFILENAME = "AmodeusOptions.properties";

    // ---
    static final String FULLCONFIGIDENTIFIER = "fullConfig";
    static final String SIMUCONFIGIDENTIFIER = "simuConfig";
    static final String VIRTUALNETWORKNAMEIDENTIFIER = "virtualNetwork";
    static final String TRAVELDATAFILENAME = "travelDataFileName";
    static final String LINKSPEEDDATAFILENAME = "linkSpeedDataFileName";
    static final String COLORSCHEMEIDENTIFIER = "colorScheme";
    static final String CHARTTHEMEIDENTIFIER = "chartTheme";
    static final String NETWORKUPDATEDNAMEIDENTIFIER = "NetworkUpdateName";
    static final String POPULATIONUPDATEDNAMEIDENTIFIER = "PopulationUpdateName";
    static final String LOCATIONSPECIDENTIFIER = "LocationSpec";
    static final String SHAPEFILEIDENTIFIER = "shapeFile";

    // ---
    public static final String PARKINGGENERATORIDENTIFIER = "parkingCapacityGenerator";
    public static final String PARKINGSPOTSTAGIDENTIFIER = "parkingSpotsTagInNetwork";
    public static final String PARKINGSTRATEGYIDENTIFIER = "parkingStrategy";
    // ---
    public static final String COMPLETEGRAPHIDENTIFIER = "completeGraph";
    public static final String POPULATIONCUTTERIDENTIFIER = "populationCutter";
    public static final String VIRTUALNETWORKCREATORIDENTIFIER = "virtualNetworkCreator";
    public static final String WAITFORCLIENTSIDENTIFIER = "waitForClients";
    public static final String NUMVNODESIDENTIFIER = "numVirtualNodes";
    public static final String MAXPOPULATIONSIZEIDENTIFIER = "maxPopulationSize";
    public static final String DTTRAVELDATAIDENTIFIER = "dtTravelData";
    public static final String LPSOLVER = "LPSolver";
    public static final String LPWEIGHTQ = "LPWeightQ";
    public static final String LPWEIGHTR = "LPWeightR";

    public static Properties getDefault() {
        Properties properties = new Properties();
        properties.setProperty(FULLCONFIGIDENTIFIER, "av_config_full.xml");
        properties.setProperty(SIMUCONFIGIDENTIFIER, "av_config.xml");
        properties.setProperty(MAXPOPULATIONSIZEIDENTIFIER, "2000");
        properties.setProperty(NUMVNODESIDENTIFIER, "10");
        properties.setProperty(DTTRAVELDATAIDENTIFIER, "3600");
        properties.setProperty(COMPLETEGRAPHIDENTIFIER, "true");
        properties.setProperty(WAITFORCLIENTSIDENTIFIER, "false");
        properties.setProperty(VIRTUALNETWORKNAMEIDENTIFIER, "virtualNetwork");
        properties.setProperty(TRAVELDATAFILENAME, "travelData");
        properties.setProperty(LINKSPEEDDATAFILENAME, "linkSpeedData");
        properties.setProperty(COLORSCHEMEIDENTIFIER, "NONE");
        properties.setProperty(CHARTTHEMEIDENTIFIER, "STANDARD");
        properties.setProperty(NETWORKUPDATEDNAMEIDENTIFIER, "preparedNetwork");
        properties.setProperty(POPULATIONUPDATEDNAMEIDENTIFIER, "preparedPopulation");
        properties.setProperty(VIRTUALNETWORKCREATORIDENTIFIER, VirtualNetworkCreators.KMEANS.name());
        properties.setProperty(POPULATIONCUTTERIDENTIFIER, PopulationCutters.NETWORKBASED.name());
        properties.setProperty(SHAPEFILEIDENTIFIER, "AbsoluteShapeFileName");
        properties.setProperty(PARKINGGENERATORIDENTIFIER, ParkingCapacityGenerators.NONE.name());
        properties.setProperty(PARKINGSPOTSTAGIDENTIFIER, "spatialAvCapacity");
        properties.setProperty(PARKINGSTRATEGYIDENTIFIER, ParkingStrategies.LP.name());
        return properties;
    }

    @Deprecated
    /** Should not be used in amodeus repository anymore. */
    public static void saveProperties(Properties prop) throws IOException {
        saveProperties(prop, new File(MultiFileTools.getDefaultWorkingDirectory(), OPTIONSFILENAME));
    }

    public static void saveProperties(File workingDirectory, Properties prop) throws IOException {
        saveProperties(prop, new File(workingDirectory, OPTIONSFILENAME));
    }

    public static void saveProperties(Properties prop, File file) throws IOException {
        String header = "This is a default config file that needs to be modified. In order to" + "work properly a LocationSpec needs to be set, e.g., LocationSpec=SANFRANCISCO \n";
        saveProperties(prop, file, header);
    }

    public static void saveProperties(Properties prop, File file, String headerString) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(file)) {
            prop.store(ostream, headerString);
        }
        FileLines.sort(file);
    }

    public static String getOptionsFileName() {
        return OPTIONSFILENAME;
    }

}
