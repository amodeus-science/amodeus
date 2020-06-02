/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

import amodeus.amodeus.view.jmapviewer.interfaces.TileSource;

/** The default "Mapnik" OSM tile source. */
public class MapnikTileSource extends CyclicTileSource {
    private static final String[] SERVER = { "a", "b", "c" };
    // ---
    /** singleton instance */
    public static final TileSource INSTANCE = new MapnikTileSource();

    /** Constructs a new {@code "Mapnik"} tile source. */
    private MapnikTileSource() {
        super("Mapnik", "https://%s.tile.openstreetmap.org", "MAPNIK", SERVER);
    }
}