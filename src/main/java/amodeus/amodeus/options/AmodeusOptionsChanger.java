/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public enum AmodeusOptionsChanger {
    ;

    /** Changes the entry with @param identifier to the @param newValue
     * in the AmodeusOptions.properties file in the @param directory
     * 
     * @throws IOException */
    public static void change(File directory, String identifier, String newValue) throws IOException {
        Properties props = new Properties();
        File file = new File(directory, ScenarioOptionsBase.OPTIONSFILENAME);
        try (InputStream inputStream = new FileInputStream(file)) {
            props.load(inputStream);
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
            props.setProperty(identifier, newValue);
            props.store(outputStream, null);
        }
    }
}
