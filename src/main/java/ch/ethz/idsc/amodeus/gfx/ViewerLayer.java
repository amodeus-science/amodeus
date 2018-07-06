/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.ethz.idsc.amodeus.net.simobj.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;
import ch.ethz.idsc.amodeus.view.jmapviewer.AmodeusHeatMap;

/** class is intended to serve as base class for extension outside of amodeus library */
public abstract class ViewerLayer {

    public static final int DEFAULT_HEIGHT = 20;

    /** reference to viewer component
     * ideally should be final, but my
     * OOP skills are insufficient */
    protected AmodeusComponent amodeusComponent;

    protected abstract void createPanel(RowPanel rowPanel);

    public final JPanel createPanel() {
        RowPanel rowPanel = new RowPanel();
        String string = getClass().getSimpleName();
        JLabel jLabel = new JLabel(string.substring(0, string.length() - 5));
        jLabel.setForeground(Color.BLUE);
        rowPanel.add(jLabel); // remove "Layer"
        createPanel(rowPanel);
        return rowPanel.jPanel;
    }

    /** called during each drawing pass to update visuals
     * 
     * @param ref non null */
    protected void prepareHeatmaps(SimulationObject ref) {
        // nothing to do here
    }

    protected abstract void paint(Graphics2D graphics, SimulationObject ref);

    /** @param graphics
     * @param ref */
    protected void hud(Graphics2D graphics, SimulationObject ref) {
        // override to add information to head up display
    }

    public List<AmodeusHeatMap> getHeatmaps() {
        return Collections.emptyList();
    }

    protected final void createHeatmapPanel(RowPanel rowPanel, String string, AmodeusHeatMap matsimHeatMap) {
        final JCheckBox jCheckBox = new JCheckBox(string);
        jCheckBox.setSelected(matsimHeatMap.getShow());
        jCheckBox.addActionListener(event -> {
            matsimHeatMap.setShow(jCheckBox.isSelected());
            amodeusComponent.repaint();
        });
        rowPanel.add(jCheckBox);
        {
            SpinnerLabel<ColorSchemes> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("color scheme of heatmap");
            spinnerLabel.setArray(ColorSchemes.values());
            spinnerLabel.setValue(matsimHeatMap.getColorSchemes());
            spinnerLabel.addSpinnerListener(cs -> {
                matsimHeatMap.setColorSchemes(cs);
                amodeusComponent.repaint();
            });
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            spinnerLabel.setMenuHover(true);
            rowPanel.add(spinnerLabel.getLabelComponent());
        }
    }

}
