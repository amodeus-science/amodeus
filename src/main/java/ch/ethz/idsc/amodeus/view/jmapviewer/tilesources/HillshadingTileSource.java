/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class HillshadingTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c" };

    public HillshadingTileSource() {
        super("Hillshading", "http://%s.tiles.wmflabs.org/hillshading", "hillshading", SERVER);
    }

}