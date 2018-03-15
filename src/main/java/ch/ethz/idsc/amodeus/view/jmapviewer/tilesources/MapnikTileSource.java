/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

/** The default "Mapnik" OSM tile source. */
public class MapnikTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c" };

    /** Constructs a new {@code "Mapnik"} tile source. */
    public MapnikTileSource() {
        super("Mapnik", "https://%s.tile.openstreetmap.org", "MAPNIK", SERVER);
    }

}