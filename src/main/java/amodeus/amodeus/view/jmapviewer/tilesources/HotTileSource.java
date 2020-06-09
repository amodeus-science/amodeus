/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

/** Humanitarian focused OSM base layer */
public class HotTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b" };

    public HotTileSource() {
        super("HotMap", "http://%s.tile.openstreetmap.fr/hot", "hot", SERVER);
    }

}