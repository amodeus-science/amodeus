/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.amodeus.aido.util.ContentType;
import ch.ethz.idsc.amodeus.aido.util.HttpDownloader;
import ch.ethz.idsc.amodeus.aido.util.Unzip;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.tensor.io.ResourceData;

public enum AidoScenarioDownload {
    ;

    /** @param key for instance "SanFrancisco"
     * @throws IOException */
    public static void download(String key) throws IOException {
        Properties properties = ResourceData.properties("/aido/scenarios.properties");
        if (properties.containsKey(key)) {
            /** chosing scenario */
            String value = properties.getProperty(key);
            System.out.println("scenario: " + value);
            /** file name is arbitrary, file will be deleted after un-zipping */
            File file = new File(MultiFileTools.getWorkingDirectory(), "scenario.zip");
            HttpDownloader.download(value, ContentType.APPLICATION_ZIP).to(file);
            Unzip.of(file, MultiFileTools.getWorkingDirectory(), true);
            file.delete();
        }
    }
}
