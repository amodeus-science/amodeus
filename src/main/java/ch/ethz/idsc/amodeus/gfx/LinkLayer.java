/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;

import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;

class Street {
    Point p1;
    Point p2;
}

public class LinkLayer extends ViewerLayer {
    private static final int MAX_COUNT = 16384 / 2;
    // ---
    private static final Color LINKCOLOR = new Color(153, 153, 102, 64);
    private boolean drawLinks = true; // false;
    private int count = 0;

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (drawLinks) {
            graphics.setColor(LINKCOLOR);
            List<Street> list = new LinkedList<>();
            for (OsmLink osmLink : amodeusComponent.db.getOsmLinks()) {
                Point p1 = amodeusComponent.getMapPosition(osmLink.getCoordFrom());
                if (p1 != null) {
                    Point p2 = amodeusComponent.getMapPositionAlways(osmLink.getCoordTo());
                    Street street = new Street();
                    street.p1 = p1;
                    street.p2 = p2;
                    list.add(street);
                    if (MAX_COUNT < list.size()) {
                        list.clear();
                        break;
                    }
                }
            }
            for (Street street : list) {
                Point p1 = street.p1;
                Point p2 = street.p2;
                if (p1.equals(p2))
                    graphics.drawOval(p1.x - 1, p1.y - 1, 3, 3);
                else
                    graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            count = list.size();
        }
    }

    @Override
    protected void hud(Graphics2D graphics, SimulationObject ref) {
        if (drawLinks) {
            if (0 < count)
                amodeusComponent.append("%5d/%5d streets", count, amodeusComponent.db.getOsmLinksSize());
            else
                amodeusComponent.append("too many streets");
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
