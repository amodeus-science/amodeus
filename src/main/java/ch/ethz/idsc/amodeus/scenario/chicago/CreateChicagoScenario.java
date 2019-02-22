package ch.ethz.idsc.amodeus.scenario.chicago;

import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

import org.matsim.pt2matsim.run.CreateDefaultOsmConfig;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import java.io.File;

public enum CreateChicagoScenario {
    ;

    public static void main(String[] args) throws Exception {
        CreateChicagoScenario.run();
    }

    // private static double[] bbox = new double[] {-88.0057, 41.5997, -87.4021, 42.0138}; // contains ca. 1.5 GB
    private static double[] bbox = new double[] { -87.7681, 41.8054, -87.5809, 41.9331 };

    public static void run() throws Exception {
        File workingDirectory = MultiFileTools.getWorkingDirectory();

        // TODO where to get the most basic files from?
        // AmodeusProperties.xml
        // av.xml
        // config.xml
        // network_pt2matsim.xml

        File osmFile = new File(workingDirectory, "map.osm");
        OsmLoader osm = new OsmLoader(bbox);
        osm.saveIfNotAlreadyExists(osmFile);
        Osm2MultimodalNetwork.run("network_pt2matsim.xml"); // TODO other format than local version

        File taxiData = ChicagoDataLoader.from("AmodeusOptions.properties", workingDirectory);
        ScenarioCreator scenarioCreator = new ScenarioCreator(workingDirectory, taxiData, DataOperator.CHICAGO_ONLINE);
        scenarioCreator.run();
    }
}
