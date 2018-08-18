/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;

import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;
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

/* package */ enum JMapTileSelector {
    ;
    public static SpinnerLabel<TileSource> create(JMapViewer jMapViewer) {
        TileSource[] tileSource = new TileSource[] { //
                MapnikTileSource.INSTANCE, //
                GrayMapnikTileSource.INSTANCE, //
                WikimediaTileSource.INSTANCE, //
                new LightCartoTileSource(), //
                new DarkCartoTileSource(), //
                new FrenchTileSource(), //
                new BlackWhiteTileSource(), //
                new WatercolorTileSource(), //
                new HotTileSource(), //
                new HikebikeTileSource(), // slow!
                new HillshadingTileSource(), // slow
                new OpenCycleTileSource(), // APIkey
                new CycleTileSource(), // APIkey
                new LandscapeTileSource(), // APIkey
        };
        SpinnerLabel<TileSource> spinnerLabel = new SpinnerLabel<>();
        spinnerLabel.setArray(tileSource);
        spinnerLabel.setIndex(0);
        spinnerLabel.addSpinnerListener(jMapViewer::setTileSource);
        spinnerLabel.getLabelComponent().setToolTipText("tile source");
        spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, 28));
        return spinnerLabel;
    }
}
