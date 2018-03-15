/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class WatercolorTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c", "d" };

    public WatercolorTileSource() {
        super("Watercolor", "http://%s.tile.stamen.com/watercolor", "watercolor", SERVER);
    }

    @Override
    public int getMaxZoom() {
        return 18;
    }
}