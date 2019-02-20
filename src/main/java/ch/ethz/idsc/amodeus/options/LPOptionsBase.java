/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.util.io.FileLines;

public enum LPOptionsBase {
    ;

    static final String OPTIONSFILENAME = "LPOptions.properties";
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

    public static void saveProperties(File workingDirectory, Properties prop) throws IOException {
        saveProperties(prop, new File(workingDirectory, OPTIONSFILENAME));
    }

    public static void saveProperties(Properties prop, File file) throws IOException {
        String header = "This is a default config file that needs to be modified. In order to work properly LPSolver needs to be set, e.g., LPSolver=timeInvariant \n";
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
