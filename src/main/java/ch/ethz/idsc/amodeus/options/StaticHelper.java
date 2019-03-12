/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.BiConsumer;

/* package */ enum StaticHelper {
    ;

    /** @return Properties object with default options and any options found in
     *         folder @param directory with simulation data and an AmodeusOptions.properties File
     * @throws IOException */
    public static Properties loadOrCreateScenarioOptions(File directory, Properties simOptions) throws IOException {
        return locateOrLoad(directory, simOptions, ScenarioOptionsBase.OPTIONSFILENAME, //
                ScenarioOptionsBase::savePropertiesToFile);
    }

    /** @return Properties object with default options and any options found in @param directory
     *         with simulation data and LPOptions.properties file
     * @throws IOException */
    public static Properties loadOrCreateLPOptions(File directory, Properties simOptions) throws IOException {
        return locateOrLoad(directory, simOptions, LPOptionsBase.OPTIONSFILENAME, //
                LPOptionsBase::savePropertiesToFile);
    }

    private static Properties locateOrLoad(File directory, Properties properties, //
            String fileName, BiConsumer<Properties, File> saveDefault) throws IOException {
        System.out.println("searching file " + fileName + //
                " in directory " + directory.getCanonicalFile());
        File simOptionsFile = new File(directory, fileName);
        if (simOptionsFile.exists()) {
            properties.load(new FileInputStream(simOptionsFile));
        } else {
            LPOptionsBase.savePropertiesToFile(properties, simOptionsFile);
        }
        return properties;
    }
}
