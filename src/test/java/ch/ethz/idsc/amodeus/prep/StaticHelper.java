package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import ch.ethz.idsc.amodeus.aido.AidoScenarioDownload;
import ch.ethz.idsc.tensor.io.ResourceData;

/* package */ class StaticHelper {

    /** chose a random AIDO scenario and download it
     * 
     * @throws Exception */
    public static void setupScenario() throws Exception {
        Properties properties = ResourceData.properties("/aido/scenarios.properties");
        List<Object> keys = new ArrayList<>();
        keys.addAll(properties.keySet());
        Collections.shuffle(keys);
        String key = (String) keys.get(0);
        System.out.println("testing scenario:  " + key);
        AidoScenarioDownload.download(key);
    }

    /** clean up downloaded files */
    public static void cleanScenario() {
        String[] files = new String[] { "AmodeusOptions.properties", "av.xml", //
                "matsimConfig.xml", "personAtrributes-with-subpopulation.xml", //
                "preparedNetwork.xml", "config_full.xml", "linkSpeedData" };
        for (String file : files) {
            boolean ok = new File(file).delete();
            if (!ok) {
                System.err.println("file: " + file + " not deleted.");
            }
        }
    }
}
