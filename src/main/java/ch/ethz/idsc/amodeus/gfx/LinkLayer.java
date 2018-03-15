/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JCheckBox;

import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;

public class LinkLayer extends ViewerLayer {

    private static final Color LINKCOLOR = new Color(153, 153, 102, 64);

    private boolean drawLinks = false;

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (drawLinks) {
            graphics.setColor(LINKCOLOR);
            for (OsmLink osmLink : amodeusComponent.db.getOsmLinks()) {
                Point p1 = amodeusComponent.getMapPosition(osmLink.getCoordFrom());
                if (p1 != null) {
                    Point p2 = amodeusComponent.getMapPositionAlways(osmLink.getCoordTo());
                    graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }

    @Override
    protected void hud(Graphics2D graphics, SimulationObject ref) {
        if (drawLinks) {
            amodeusComponent.append("%5d streets", amodeusComponent.db.getOsmLinksSize());
            amodeusComponent.appendSeparator();
        }
    }

    public void setDraw(boolean selected) {
        drawLinks = selected;
        amodeusComponent.repaint();
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        final JCheckBox jCheckBox = new JCheckBox("streets");
        jCheckBox.setToolTipText("each link as thin line");
        jCheckBox.setSelected(drawLinks);
        jCheckBox.addActionListener(event -> setDraw(jCheckBox.isSelected()));
        rowPanel.add(jCheckBox);
    }

}
