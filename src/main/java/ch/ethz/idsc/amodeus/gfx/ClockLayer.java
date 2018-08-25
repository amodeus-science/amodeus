/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JCheckBox;

import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.GraphicsUtil;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;

/** Head Up Display */
public class ClockLayer extends ViewerLayer {

    public boolean show = true;
    /** opacity of clock background */
    public int alpha = 255;

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (show) {
            final Dimension dimension = amodeusComponent.getSize();
            GraphicsUtil.setQualityHigh(graphics);
            IdscClockDisplay.INSTANCE.drawClock(graphics, ref.now, new Point(dimension.width - 70, 70), alpha);
            GraphicsUtil.setQualityDefault(graphics);
        }
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        JCheckBox jCheckBox = new JCheckBox("clock");
        jCheckBox.setToolTipText("time on clock");
        jCheckBox.setSelected(show);
        jCheckBox.addActionListener(e -> setShow(jCheckBox.isSelected()));
        rowPanel.add(jCheckBox);
    }

    private void setShow(boolean selected) {
        show = selected;
        amodeusComponent.repaint();
    }
}
