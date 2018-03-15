/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.jmapviewer.tilesources;

public class DarkCartoTileSource extends CartoTileSource {

    public DarkCartoTileSource() {
        super("CartoDark", "http://%s.basemaps.cartocdn.com/dark_all", "cartodark");
    }

}