/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;
import java.awt.Graphics2D;

import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;

/** Head Up Display */
public class HudLayer extends ViewerLayer {

    public boolean show = true;

    public HudLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        // ---
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {

        SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
        spinnerLabel.setArray(0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24);
        spinnerLabel.setMenuHover(true);
        spinnerLabel.setValueSafe(amodeusComponent.getFontSize());
        spinnerLabel.addSpinnerListener(i -> {
            amodeusComponent.setFontSize(i);
            amodeusComponent.repaint();
        });
        spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
        spinnerLabel.getLabelComponent().setToolTipText("font size of info");
        rowPanel.add(spinnerLabel.getLabelComponent());

    }

    @Override
    public void updateSettings(ViewerSettings settings) {
        settings.infoFontSize = amodeusComponent.getFontSize();
    }

    @Override
    public void loadSettings(ViewerSettings settings) {
        amodeusComponent.setFontSize(settings.infoFontSize);
    }

}
