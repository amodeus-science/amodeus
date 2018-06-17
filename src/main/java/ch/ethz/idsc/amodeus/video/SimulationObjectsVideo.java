/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.gfx.AmodeusComponent;
import ch.ethz.idsc.amodeus.net.IterationFolder;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.StorageSupplier;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.view.jmapviewer.Coordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworkGet;

/** @author onicolo 06-2018
 *         Runnable script for making video renderers of Amod simulation directly from simulation objects. */
public class SimulationObjectsVideo {
    public static final Dimension RESOLUTION_4K = new Dimension(3840, 2160);

    private final File workingDirectory;
    private final String output_subfolder;
    private final File simOutputDir;
    private AmodeusComponent amodeusComponent;
    private ScenarioOptions simOptions;
    private Network network;
    private VirtualNetwork<Link> vNetwork;
    private int skip = 1;

    /** Hint: the working directory has to contain the files config.xml, AmodeusOptions.properties, network.xml.gz etc.
     * 
     * @param output_subfolder for instance "output/001" relative to the working directory
     * @throws IOException */
    public SimulationObjectsVideo(String output_subfolder) throws IOException {
        workingDirectory = MultiFileTools.getWorkingDirectory();
        this.output_subfolder = output_subfolder;
        simOutputDir = new File(workingDirectory, output_subfolder);
        System.out.println(simOutputDir);
        GlobalAssert.that(simOutputDir.isDirectory());

        loadScenario();
    }

    public void setSkip(int skip) {
        this.skip = Math.max(1, skip);
    }

    /** @param dimension
     * @param fps
     * @throws Exception */
    public void make(Dimension dimension, int fps) throws Exception {
        StorageUtils storageUtils = new StorageUtils(simOutputDir);
        String video_name = output_subfolder.replace(File.separatorChar, '_') + ".mp4";
        String filename = new File(workingDirectory, video_name).toString();

        try (Mp4AnimationWriter mp4 = new Mp4AnimationWriter(filename, dimension, fps)) {

            // Get first MATSim iteration folder
            IterationFolder iterationFolder = storageUtils.getAvailableIterations().get(0);
            StorageSupplier storageSupplier = iterationFolder.storageSupplier();

            amodeusComponent.setSize(dimension);

            adjustMapZoom();

            final BufferedImage bufferedImage = new BufferedImage(amodeusComponent.getWidth(), amodeusComponent.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            final Graphics2D graphics = bufferedImage.createGraphics();

            int count = 1;
            for (int i = 0; i < storageSupplier.size(); i += skip) {
                amodeusComponent.setSimulationObject(storageSupplier.getSimulationObject(i));

                StaticHelper.setQualityHigh(graphics);
                amodeusComponent.paint(graphics);

                if (i == 0) {
                    System.out.println("Loading Tile..");
                    long ct = 0;
                    long prevt = System.currentTimeMillis();
                    int millis = 1000;
                    // Quick sol: Wait 1 minute for tile loading
                    while (ct <= prevt + millis) {
                        ct = System.currentTimeMillis();
                    }
                    System.out.println("..Done! -> Starting .mp4 Video..");
                }

                mp4.append(bufferedImage);

                if (i > count) {
                    System.out.println("We are at simObj: " + count);
                    count *= 2;
                }
            }
            System.out.println("..Recorded!");

        }

    }

    private void adjustMapZoom() {
        // TODO Make centering wrt virtual network
        // Collection<Link> cl = (Collection<Link>) network.getLinks().values();
        // Map<VirtualNode<Link>, List<Link>> mpLi = vNetwork.binToVirtualNode(cl, Function.identity());
        // Bounding box -> {minX, minY, maxX, maxY}
        double[] bBox = NetworkUtils.getBoundingBox(network.getNodes().values());
        CoordinateTransformation ct = simOptions.getLocationSpec().referenceFrame().coords_toWGS84();
        Coord northWest = ct.transform(new Coord(bBox[0], bBox[3]));
        Coord southWest = ct.transform(new Coord(bBox[0], bBox[1]));
        Coord southEast = ct.transform(new Coord(bBox[2], bBox[1]));
        Coord northEast = ct.transform(new Coord(bBox[2], bBox[3]));
        Set<Coord> envelop = new HashSet<>();
        envelop.add(northEast);
        envelop.add(southWest);
        envelop.add(northWest);
        envelop.add(southEast);

        // Coord center3 = new Coord(northWest.getX() + (northEast.getX() - northWest.getX()) / 2, southWest.getY() + (northWest.getY() - southWest.getY()) / 2);

        Coord center2 = MatsimStaticDatabase.INSTANCE.getCenter();
        boolean zoomAdj = false;
        boolean zoomAdjPrev = false;
        int prevZoom = 10;
        int width = amodeusComponent.getWidth();
        int height = amodeusComponent.getHeight();
        Point ccc = new Point(width / 2, height / 2);
        ((JMapViewer) amodeusComponent).setDisplayPosition(ccc, new Coordinate(center2.getY(), center2.getX()), prevZoom);
        while (!zoomAdj) {
            if (envelop.stream().allMatch(c -> ((JMapViewer) amodeusComponent).getMapPosition(c.getY(), c.getX(), true) != null)) {
                prevZoom = prevZoom + 1;
                zoomAdjPrev = true;
            } else {
                prevZoom = prevZoom - 1;
                zoomAdj = (zoomAdjPrev) ? true : false;
                zoomAdjPrev = (zoomAdj) ? true : false;
            }
            ((JMapViewer) amodeusComponent).setDisplayPosition(ccc, new Coordinate(center2.getY(), center2.getX()), prevZoom);
        }
    }

    // Taken from ScenarioViewer
    private void loadScenario() throws IOException {

        // load options
        simOptions = ScenarioOptions.load(workingDirectory);
        Config config = ConfigUtils.loadConfig(simOptions.getSimulationConfigName());
        final File outputSubDirectory = new File(config.controler().getOutputDirectory()).getAbsoluteFile();
        GlobalAssert.that(outputSubDirectory.isDirectory());
        System.out.println(outputSubDirectory.getAbsolutePath());
        // FIXME This dous only work if a sub directory is present.
        // File outputDirectory = outputSubDirectory.getParentFile();

        ReferenceFrame referenceFrame = simOptions.getLocationSpec().referenceFrame();
        /** reference frame needs to be set manually in IDSCOptions.properties file */

        // Update AMODEUS so that networkUtils can load .v2dtd networks. -> Already present in Amodidsc
        network = StaticHelper.loadNetwork(new File(workingDirectory, config.network().getInputFile()));

        GlobalAssert.that(Objects.nonNull(network));

        System.out.println("INFO network loaded");
        System.out.println("INFO total links " + network.getLinks().size());
        System.out.println("INFO total nodes " + network.getNodes().size());

        // load viewer
        MatsimStaticDatabase.initializeSingletonInstance(network, referenceFrame);
        amodeusComponent = AmodeusComponent.createDefault(MatsimStaticDatabase.INSTANCE);

        /** this is optional and should not cause problems if file does not
         * exist. temporary solution */
        vNetwork = VirtualNetworkGet.readDefault(network);

        amodeusComponent.virtualNetworkLayer.setVirtualNetwork(vNetwork);

    }

}
