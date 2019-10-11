/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.util.Arrays;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.BlackWhiteTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.CycleTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.DarkCartoTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.FrenchTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.GrayMapnikTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.HikebikeTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.HillshadingTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.HotTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.LandscapeTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.LightCartoTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.MapnikTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.OpenCycleTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.WatercolorTileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.WikimediaTileSource;

/* package */ enum MapSource {
    Wikimedia(WikimediaTileSource.INSTANCE), //
    CartoLight(new LightCartoTileSource()), //
    CartoDark(new DarkCartoTileSource()), //
    FrenchMap(new FrenchTileSource()), //
    BlackWhite(new BlackWhiteTileSource()), //
    Watercolor(new WatercolorTileSource()), //
    HotMap(new HotTileSource()), //
    Hikebike(new HikebikeTileSource()), // slow!
    Hillshading(new HillshadingTileSource()), // slow
    Mapnik(MapnikTileSource.INSTANCE), //
    GrayMapnik(GrayMapnikTileSource.INSTANCE), //
    OpenCycle(new OpenCycleTileSource()), // APIkey
    Cyclemap(new CycleTileSource()), // APIkey
    Landscape(new LandscapeTileSource()), // APIkey
    ;

    private TileSource tileSource;

    MapSource(TileSource tileSource) {
        this.tileSource = tileSource;
        GlobalAssert.that(this.tileSource.getName().equals(name()));
    }

    public TileSource getTileSource() {
        return tileSource;
    }

    public static TileSource[] array() {
        return Arrays.stream(MapSource.values()).map(s -> s.tileSource).toArray(TileSource[]::new);
    }
}