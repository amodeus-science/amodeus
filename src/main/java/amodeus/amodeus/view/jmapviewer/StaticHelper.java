// License: GPL. For details, see Readme.txt file.
package amodeus.amodeus.view.jmapviewer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import amodeus.amodeus.view.jmapviewer.interfaces.TileSource;

/* package */ enum StaticHelper {
    ;

    static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(JMapViewer.class.getResourceAsStream(path));
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("cannot load " + path);
            return null;
        }
    }

    static String getTileKey(TileSource source, int xtile, int ytile, int zoom) {
        return zoom + "/" + xtile + "/" + ytile + "@" + source.getName();
    }

}
