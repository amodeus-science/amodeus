/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class FrenchTileSource extends CyclicTileSource {

    private static final String[] SERVER = { "a", "b", "c" };

    public FrenchTileSource() {
        super("FrenchMap", "http://%s.tile.openstreetmap.fr/osmfr", "french", SERVER);
    }

}