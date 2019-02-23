package ch.ethz.idsc.amodeus.scenario.chicago;

import java.io.File;
import java.util.Arrays;

import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.scenario.DataOperator;
import ch.ethz.idsc.amodeus.scenario.OsmLoader;
import ch.ethz.idsc.amodeus.scenario.Pt2MatsimXML;
import ch.ethz.idsc.amodeus.scenario.ScenarioCreator;
import ch.ethz.idsc.amodeus.util.io.CopySomeFiles;
import ch.ethz.idsc.amodeus.util.io.FileDelete;
import ch.ethz.idsc.amodeus.util.io.LocateUtils;

/* package */ enum CreateChicagoScenario {
    ;

    /** @param args working directory (empty directory), will create an AMoDeus scenario based on
     *            the Chicago taxi dataset available online.
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        CreateChicagoScenario.run(new File(args[0]));
    }

    // TODO change for discommented larger test map
    // private static double[] bbox = new double[] {-88.0057, 41.5997, -87.4021, 42.0138}; // contains ca. 1.5 GB
    private static double[] bbox = new double[] { -87.7681, 41.8054, -87.5809, 41.9331 };

    public static void run(File workingDirectory) throws Exception {
        ChicagoGeoInformation.setup();

        /** copy relevant files containing settings for scenario generation */
        File scenarioDirectory = new File(LocateUtils.getSuperFolder("amodeus"), "resources/chicagoScenario");
        CopySomeFiles.now(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath(), //
                Arrays.asList(new String[] { "AmodeusOptions.properties", //
                        "av.xml", "config_full.xml", "network_pt2matsim.xml" }));

        Pt2MatsimXML.toLocalFileSystem(new File(workingDirectory, "network_pt2matsim.xml"), workingDirectory.getAbsolutePath());

        /** download of open street map data to create scenario */
        System.out.println("Downloading open stret map data, this may take a while...");
        File osmFile = new File(workingDirectory, "map.osm");
        OsmLoader osm = new OsmLoader(bbox);
        osm.saveIfNotAlreadyExists(osmFile);

        /** generate a network using pt2Matsim */
        Osm2MultimodalNetwork.run(workingDirectory.getAbsolutePath() + "/network_pt2matsim.xml"); // TODO other format than local version

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        File taxiData = ChicagoDataLoader.from("AmodeusOptions.properties", workingDirectory);
        ScenarioCreator scenarioCreator = new ScenarioCreator(workingDirectory, taxiData, DataOperator.CHICAGO_ONLINE);
        scenarioCreator.run(workingDirectory);

        /** delete unneeded files */
        FileDelete.of(new File(workingDirectory, "Scenario"), 2, 14);
        FileDelete.of(new File(workingDirectory, "AmodeusOptions.properties"), 0, 1);
        FileDelete.of(new File(workingDirectory, "av.xml"), 0, 1);
        FileDelete.of(new File(workingDirectory, "config_full.xml"), 0, 1);
        FileDelete.of(new File(workingDirectory, "network_pt2matsim.xml"), 0, 1);
        FileDelete.of(new File(workingDirectory, "network.xml"), 0, 1);

    }
}
