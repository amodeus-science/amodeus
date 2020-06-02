/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.options;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import amodeus.amodeus.util.io.FileLines;

public enum LPOptionsBase {
    ;

    public static final String OPTIONSFILENAME = "LPOptions.properties";
    // ---
    public static final String LPSOLVER = "LPSolver";
    public static final String LPWEIGHTQ = "LPWeightQ";
    public static final String LPWEIGHTR = "LPWeightR";

    public static Properties getDefault() {
        Properties properties = new Properties();
        properties.setProperty(LPSOLVER, "TIMEINVARIANT");
        properties.setProperty(LPWEIGHTQ, "0.99");
        properties.setProperty(LPWEIGHTR, "0.01");
        return properties;
    }

    public static void savePropertiesToDirectory(File workingDirectory, Properties prop) {
        savePropertiesToFile(prop, new File(workingDirectory, OPTIONSFILENAME));
    }

    public static void savePropertiesToFile(Properties prop, File file) {
        String header = "This is a default config file that needs to be modified. In order" //
                + " to work properly LPSolver needs to be set, e.g., LPSolver=timeInvariant \n";
        savePropertiesToFileWithHeader(prop, file, header);
    }

    public static void savePropertiesToFileWithHeader(Properties prop, File file, String headerString) {
        try (FileOutputStream ostream = new FileOutputStream(file)) {
            prop.store(ostream, headerString);
            FileLines.sort(file);
        } catch (Exception exception) {
            System.err.println("Could not save file " + file.getName() + ", in amodeus.amodeus.options.LPOptionsBase");
        }
    }
}
