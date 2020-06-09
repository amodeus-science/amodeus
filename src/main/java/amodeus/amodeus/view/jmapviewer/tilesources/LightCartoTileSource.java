/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

public class LightCartoTileSource extends CartoTileSource {

    public LightCartoTileSource() {
        super("CartoLight", "http://%s.basemaps.cartocdn.com/light_all", "CartoLight");
    }
}