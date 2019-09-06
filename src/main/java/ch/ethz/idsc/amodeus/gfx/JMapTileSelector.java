/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;

import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;

/* package */ enum JMapTileSelector {
    ;
    public static SpinnerLabel<TileSource> create(JMapViewer jMapViewer) {
        TileSource[] tileSource = MapSource.array();
        SpinnerLabel<TileSource> spinnerLabel = new SpinnerLabel<>();
        spinnerLabel.setArray(tileSource);
        spinnerLabel.setIndex(0);
        spinnerLabel.addSpinnerListener(jMapViewer::setTileSource);
        spinnerLabel.getLabelComponent().setToolTipText("tile source");
        spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, 28));
        return spinnerLabel;
    }
}
