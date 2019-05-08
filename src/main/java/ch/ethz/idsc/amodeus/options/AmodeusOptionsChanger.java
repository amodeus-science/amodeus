package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AmodeusOptionsChanger {

    /** Changes the entry with @param identifier to the @param newValue
     * in the AmodeusOptions.properties file in the @param directory
     * 
     * @throws IOException */
    public static void change(File directory, String identifier, String newValue) throws IOException {
        Properties props = new Properties();
        File propsFile = new File(directory, ScenarioOptionsBase.OPTIONSFILENAME);
        try (FileInputStream in = new FileInputStream(propsFile)) {
            props.load(in);
        }

        try (FileOutputStream out = new FileOutputStream(propsFile)) {
            props.setProperty(identifier, newValue);
            props.store(out, null);
        }
    }
}
