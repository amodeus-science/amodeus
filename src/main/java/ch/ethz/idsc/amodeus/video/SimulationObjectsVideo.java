/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;

import ch.ethz.idsc.amodeus.gfx.AmodeusComponent;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.GraphicsUtil;

/** @author onicolo 06-2018
 *         Runnable script for making video renderers of Amod simulation directly from simulation objects. */
public class SimulationObjectsVideo implements AutoCloseable {
    public static final Dimension RESOLUTION_4K = new Dimension(3840, 2160);
    public static final Dimension RESOLUTION_FullHD = new Dimension(1920, 1080);
    // ---
    private final Mp4AnimationWriter mp4;
    private final BufferedImage bufferedImage;
    private final Graphics2D graphics;
    // ---
    private final AmodeusComponent amodeusComponent;

    /** Hint: the working directory has to contain the files config.xml, AmodeusOptions.properties, network.xml.gz etc.
     * 
     * @param filename for instance "video.mp4" relative to the working directory
     * @throws Exception */
    public SimulationObjectsVideo(String filename, Dimension dimension, int fps, AmodeusComponent amodeusComponent) throws Exception {

        mp4 = new Mp4AnimationWriter(filename, dimension, fps);

        this.amodeusComponent = amodeusComponent;
        amodeusComponent.setSize(dimension);

        bufferedImage = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_3BYTE_BGR);
        graphics = bufferedImage.createGraphics();
    }

    private boolean first = true;
    public int millis = 8000;

    public void append(SimulationObject simulationObject) throws Exception {

        amodeusComponent.setSimulationObject(simulationObject);

        GraphicsUtil.setQualityHigh(graphics);
        amodeusComponent.paint(graphics);

        if (first) {
            first &= false;
            // TODO MISC tile loading method is not foolproof
            System.out.println("Loading Tiles...");
            Thread.sleep(millis);
            System.out.println("Starting mp4 Video.");

            GraphicsUtil.setQualityHigh(graphics);
            amodeusComponent.paint(graphics);
        }

        mp4.append(bufferedImage);

    }

    @Override // from AutoCloseable
    public void close() throws Exception {
        if (Objects.nonNull(mp4))
            mp4.close();
    }

}
