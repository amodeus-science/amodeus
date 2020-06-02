/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

public class OpenCycleTileSource extends CyclicTileSource {
    private static final String[] SERVER = { "a", "b", "c" };

    public OpenCycleTileSource() {
        super("OpenCycle", "http://%s.tile2.opencyclemap.org/transport", "OpenCycle", SERVER);
    }

}