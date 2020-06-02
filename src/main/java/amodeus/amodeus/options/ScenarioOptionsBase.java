/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.options;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import amodeus.amodeus.parking.ParkingCapacityGenerators;
import amodeus.amodeus.parking.strategies.ParkingStrategies;
import amodeus.amodeus.prep.PopulationCutters;
import amodeus.amodeus.prep.VirtualNetworkCreators;
import amodeus.amodeus.util.io.FileLines;

public enum ScenarioOptionsBase {
    ;

    public static final String OPTIONSFILENAME = "AmodeusOptions.properties";

    // ---
    /* package */ static final String FULLCONFIGIDENTIFIER = "fullConfig";
    /* package */ static final String SIMUCONFIGIDENTIFIER = "simuConfig";
    /* package */ static final String VIRTUALNETWORKNAMEIDENTIFIER = "virtualNetwork";
    /* package */ static final String TRAVELDATAFILENAME = "travelDataFileName";
    /* package */ static final String LINKSPEEDDATAFILENAME = "linkSpeedDataFileName";
    /* package */ static final String COLORSCHEMEIDENTIFIER = "colorScheme";
    /* package */ static final String CHARTTHEMEIDENTIFIER = "chartTheme";
    /* package */ static final String NETWORKUPDATEDNAMEIDENTIFIER = "NetworkUpdateName";
    /* package */ static final String POPULATIONUPDATEDNAMEIDENTIFIER = "PopulationUpdateName";
    /* package */ static final String LOCATIONSPECIDENTIFIER = "LocationSpec";
    /* package */ static final String SHAPEFILEIDENTIFIER = "shapeFile";

    // ---
    public static final String PARKINGGENERATORIDENTIFIER = "parkingCapacityGenerator";
    public static final String PARKINGSPOTSTAGIDENTIFIER = "parkingSpotsTagInNetwork";
    public static final String PARKINGSTRATEGYIDENTIFIER = "parkingStrategy";
    public static final String PARKINGCONSTANTCAPACITY = "constantParkingCapacity";
    public static final String PARKINGTOTALSPACES = "totalParkingSpaces";
    public static final String PARKINGLENGTHDENSITY = "parkingCapacityPerLength";
    public static final String PARKINGMINlINKCAPACITY = "minLinkParkingCapacity";

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
    public static final String RANDOMSEED = "RandomSeed";

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

    public static void savePropertiesToDirectory(File workingDirectory, Properties prop) {
        savePropertiesToFile(prop, new File(workingDirectory, OPTIONSFILENAME));
    }

    public static void savePropertiesToFile(Properties prop, File file) {
        String header = "This is a default config file that needs to be modified. In order to" + //
                "work properly a LocationSpec needs to be set, e.g., LocationSpec=SANFRANCISCO \n";
        savePropertiesToFileWithHeader(prop, file, header);
    }

    public static void savePropertiesToFileWithHeader(Properties prop, File file, String headerString) {
        try (FileOutputStream ostream = new FileOutputStream(file)) {
            prop.store(ostream, headerString);
            FileLines.sort(file);
        } catch (Exception exception) {
            System.err.println("Could not save file " + file.getAbsolutePath() + " in amodeus.amodeus.options.ScenarioOptionsBase");
        }
    }
}
