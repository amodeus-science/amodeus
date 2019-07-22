/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import junit.framework.TestCase;

public class Mp4AnimationWriterTest extends TestCase {
    private final static File MP4_FILE = new File(HomeDirectory.file("filename.mp4").toString());

    /** test generates short video of 50 frames with random colors per pixel at 10 fps */
    public void testSimple() throws InterruptedException, IOException, Exception {

        /** Read in some option values and their defaults. */
        final int snaps = 10; // fps

        final String filename = MP4_FILE.toString();

        Dimension dimension = new Dimension(640, 480);
        try (Mp4AnimationWriter mp4 = new Mp4AnimationWriter(filename, dimension, snaps)) {
            BufferedImage bufferedImage = //
                    new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_3BYTE_BGR);
            WritableRaster writableRaster = bufferedImage.getRaster();
            DataBufferByte dataBufferByte = (DataBufferByte) writableRaster.getDataBuffer();

            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                /** Make the screen capture && convert image to TYPE_3BYTE_BGR */
                random.nextBytes(dataBufferByte.getData());
                mp4.append(bufferedImage);
            }
        }

    }

    @Override
    public void tearDown() throws IOException {
        if (MP4_FILE.exists())
            DeleteDirectory.of(MP4_FILE, 1, 1);
    }
}
