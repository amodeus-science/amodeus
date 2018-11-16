package ch.ethz.idsc.amodeus.video;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.ext.Static;
import ch.ethz.idsc.amodeus.gfx.*;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.*;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.GrayMapnikTileSource;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import java.awt.*;
import java.io.File;
import java.util.Objects;

public class VideoGenerator implements Runnable {
    Thread thread;
    File workingDirectory;

    public VideoGenerator(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void start() {
        thread = new Thread(this);
        System.out.println("starting video generation");
        thread.start();
    }

    public void run() {
        Static.setup();
        try {
            // load options
            ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
            Config config = ConfigUtils.loadConfig(scenarioOptions.getSimulationConfigName());
            final File outputSubDirectory = new File(config.controler().getOutputDirectory()).getAbsoluteFile();
            GlobalAssert.that(outputSubDirectory.isDirectory());

            ReferenceFrame referenceFrame = scenarioOptions.getLocationSpec().referenceFrame();
            /** reference frame needs to be set manually in IDSCOptions.properties file */

            Network network = NetworkLoader.fromNetworkFile(new File(workingDirectory, config.network().getInputFile()));

            export(network, referenceFrame, scenarioOptions, outputSubDirectory);

            System.out.println("successfully finished video generation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void export(Network network, ReferenceFrame referenceFrame, //
                              ScenarioOptions scenarioOptions, File outputSubDirectory) throws Exception {

        GlobalAssert.that(Objects.nonNull(network));

        System.out.println("INFO network loaded");
        System.out.println("INFO total links " + network.getLinks().size());
        System.out.println("INFO total nodes " + network.getNodes().size());

        // load viewer
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        AmodeusComponent amodeusComponent = new AmodeusComponent(db);
        ViewerConfig viewerConfig = ViewerConfig.from(db, workingDirectory);

        amodeusComponent.setTileSource(viewerConfig.getTileSource());

        TilesLayer tilesLayer = new TilesLayer(amodeusComponent);
        tilesLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(tilesLayer);

        VehiclesLayer vehiclesLayer = new VehiclesLayer(amodeusComponent);
        vehiclesLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(vehiclesLayer);

        RequestsLayer requestsLayer = new RequestsLayer(amodeusComponent);
        requestsLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(requestsLayer);

        LinkLayer linkLayer = new LinkLayer(amodeusComponent);
        // linkLayer.linkLimit = 16384; // might wanna increase link limit to compensate dimensions
        linkLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(linkLayer);

        LoadLayer loadLayer = new LoadLayer(amodeusComponent);
        loadLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(loadLayer);

        HudLayer hudLayer = new HudLayer(amodeusComponent);
        hudLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(hudLayer);

        ClockLayer clockLayer = new ClockLayer(amodeusComponent);
        clockLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(clockLayer);

        /** this is optional and should not cause problems if file does not
         * exist. temporary solution */
        VirtualNetwork<Link> virtualNetwork = VirtualNetworkGet.readDefault(network); // may be null
        System.out.println("has vn: " + (virtualNetwork != null));
        VirtualNetworkLayer virtualNetworkLayer = new VirtualNetworkLayer(amodeusComponent);
        virtualNetworkLayer.setVirtualNetwork(virtualNetwork);
        virtualNetworkLayer.loadSettings(viewerConfig.settings);
        amodeusComponent.addLayer(virtualNetworkLayer);

        Dimension resolution = SimulationObjectsVideo.RESOLUTION_FullHD;
        amodeusComponent.setSize(resolution);
        AmodeusComponentUtil.adjustMapZoom(amodeusComponent, network, scenarioOptions, db);
        amodeusComponent.reorientMap(viewerConfig);

        StorageUtils storageUtils = new StorageUtils(outputSubDirectory);
        IterationFolder iterationFolder = storageUtils.getAvailableIterations().get(0);
        // storageSupplier typically has size = 10800
        StorageSupplier storageSupplier = iterationFolder.storageSupplier();

        int count = 0;
        int base = 1;
        try (SimulationObjectsVideo simulationObjectsVideo = new SimulationObjectsVideo( //
                String.format("%s_%s.mp4", java.time.LocalDate.now(), network.getName()), // TODO meaningful naming
                resolution, viewerConfig.settings.fps, amodeusComponent //
        )) {
            simulationObjectsVideo.millis = 20000;
            int intervalEstimate = storageSupplier.getIntervalEstimate(); // 10
            int hrs = 60 * 60 / intervalEstimate;
            final int start = viewerConfig.settings.startTime * hrs;
            final int end = Math.min(viewerConfig.settings.endTime * hrs, storageSupplier.size());
            for (int index = start; index < end; index += 1) {
                SimulationObject simulationObject = storageSupplier.getSimulationObject(index);
                simulationObjectsVideo.append(simulationObject);
                if (++count >= base) {
                    System.out.println("render simObj " + count + "/" + (end - start));
                    base *= 2;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
