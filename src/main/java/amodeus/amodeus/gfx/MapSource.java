/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import java.util.Arrays;

import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.view.jmapviewer.interfaces.TileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.BlackWhiteTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.CycleTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.DarkCartoTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.FrenchTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.GrayMapnikTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.HikebikeTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.HillshadingTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.HotTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.LandscapeTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.LightCartoTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.MapnikTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.OpenCycleTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.WatercolorTileSource;
import amodeus.amodeus.view.jmapviewer.tilesources.WikimediaTileSource;

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