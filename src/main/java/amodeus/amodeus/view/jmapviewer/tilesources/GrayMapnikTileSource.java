/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

import amodeus.amodeus.view.jmapviewer.interfaces.TileSource;

public class GrayMapnikTileSource extends CyclicTileSource {
    private static final String[] SERVER = { "a", "b", "c" };
    // ---
    /** singleton instance */
    public static final TileSource INSTANCE = new GrayMapnikTileSource();

    private GrayMapnikTileSource() {
        super("GrayMapnik", "http://%s.tiles.wmflabs.org/bw-mapnik", "GrayMapnik", SERVER);
    }
}