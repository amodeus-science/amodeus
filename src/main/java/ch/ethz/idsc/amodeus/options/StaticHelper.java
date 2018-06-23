/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

enum StaticHelper {
    ;

    /** @param directory
     *            with simulation data an dan IDSC.Options.properties file
     * @return Properties object with default options and any options found in
     *         folder
     * @throws IOException */
    static Properties loadOrCreate(File directory, Properties simOptions) throws IOException {
        System.out.println("working in directory \n" + directory.getCanonicalFile());

        File simOptionsFile = new File(directory, ScenarioOptionsBase.OPTIONSFILENAME);
        if (simOptionsFile.exists()) {
            simOptions.load(new FileInputStream(simOptionsFile));
        } else
            ScenarioOptionsBase.saveProperties(simOptions, simOptionsFile);

        return simOptions;
    }

}
