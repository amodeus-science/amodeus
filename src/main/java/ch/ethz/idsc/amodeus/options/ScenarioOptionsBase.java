/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.prep.PopulationCutters;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkCreators;

public class ScenarioOptionsBase {

    private static final String OPTIONSFILENAME = "AmodeusOptions.properties";

    // ---
    public static final String FULLCONFIGIDENTIFIER = "fullConfig";
    public static final String SIMUCONFIGIDENTIFIER = "simuConfig";
    public static final String LOCATIONSPECIDENTIFIER = "LocationSpec";
    public static final String VIRTUALNETWORKNAMEIDENTIFIER = "virtualNetwork";
    public static final String TRAVELDATAFILENAME = "travelDataFileName";
    public static final String LINKSPEEDDATAFILENAME = "linkSpeedDataFileName";
    public static final String COLORSCHEMEIDENTIFIER = "colorScheme";
    public static final String CHARTTHEMEIDENTIFIER = "chartTheme";
    public static final String DTTRAVELDATAIDENTIFIER = "dtTravelData";
    public static final String NUMVNODESIDENTIFIER = "numVirtualNodes";
    public static final String COMPLETEGRAPHIDENTIFIER = "completeGraph";
    public static final String NETWORKUPDATEDNAMEIDENTIFIER = "NetworkUpdateName";
    public static final String POPULATIONUPDATEDNAMEIDENTIFIER = "PopulationUpdateName";
    public static final String POPULATIONCUTTERIDENTIFIER = "populationCutter";
    public static final String MAXPOPULATIONSIZEIDENTIFIER = "maxPopulationSize";
    public static final String VIRTUALNETWORKCREATORIDENTIFIER = "virtualNetworkCreator";
    public static final String WAITFORCLIENTSIDENTIFIER = "waitForClients";
    public static final String SHAPEFILEIDENTIFIER = "shapeFile";

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
        return properties;
    }

    public static void saveDefault() throws IOException {
        saveProperties(getDefault());
    }

    protected static void saveProperties(Properties prop) throws IOException {
        File defaultFile = new File(OPTIONSFILENAME);
        String header = "This is a default config file that needs to be modified. In order to" + "work properly a LocationSpec needs to be set, e.g., LocationSpec=SANFRANCISCO \n";
        saveProperties(prop, defaultFile, header);
    }

    /* package */ static void saveProperties(Properties prop, File file, String headerString) throws IOException {
        try (FileOutputStream ostream = new FileOutputStream(file)) {
            prop.store(ostream, headerString);
        }
        PropertiesUtils.sortPropertiesAlphabetically(file);
    }

    /** @param workingDirectory
     *            with simulation data an dan IDSC.Options.properties file
     * @return Properties object with default options and any options found in
     *         folder
     * @throws IOException */
    public static Properties load(File workingDirectory) throws IOException {
        System.out.println("working in directory \n" + workingDirectory.getCanonicalFile());

        Properties simOptions = new Properties(getDefault());
        File simOptionsFile = new File(workingDirectory, OPTIONSFILENAME);
        if (simOptionsFile.exists()) {
            simOptions.load(new FileInputStream(simOptionsFile));
        } else
            ScenarioOptionsBase.saveDefault();

        return simOptions;
    }

    public static String getOptionsFileName() {
        return OPTIONSFILENAME;
    }

}
