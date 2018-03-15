/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class LandscapeTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c" };

    public LandscapeTileSource() {
        super("Landscape", "https://%s.tile.thunderforest.com/landscape", "landscape", SERVER);
    }

}