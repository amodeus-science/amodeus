package ch.ethz.idsc.amodeus.prep;

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
}
