/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class GrayMapnikTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c" };

    public GrayMapnikTileSource() {
        super("GrayMapnik", "http://%s.tiles.wmflabs.org/bw-mapnik", "GrayMapnik", SERVER);
    }

}