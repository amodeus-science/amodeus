/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.GraphicsUtil;
import ch.ethz.idsc.amodeus.util.gui.LazyMouse;
import ch.ethz.idsc.amodeus.util.gui.LazyMouseListener;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdEntry;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.ArcTan;

class Street {
    final OsmLink osmLink;
    Point p1;
    Point p2;

    public Street(OsmLink osmLink) {
        this.osmLink = osmLink;
    }

    Point midpoint() {
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    Tensor getLocation() {
        Point mid = midpoint();
        return Tensors.vector(mid.x, mid.y);
    }

    double angle() {
        return ArcTan.of(p2.x - p1.x, p2.y - p1.y).number().doubleValue();
    }
}

public class LinkLayer extends ViewerLayer {
    private static final int MAX_COUNT = 16384 / 2;
    // ---
    private static final Color LINKCOLOR = new Color(153, 153, 102, 64);
    private boolean drawLinks = true; // false;
    private boolean drawLabel = true; // false;
    private final JTextArea jTextArea = new JTextArea(2, 10);
    private int count = 0;

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {

        if (drawLinks) {
            List<Street> list = new LinkedList<>();
            Dimension dimension = amodeusComponent.getSize();
            NdTreeMap<Street> map = new NdTreeMap<>(Array.zeros(2), Tensors.vector(dimension.width, dimension.height), 12, 8);
            for (OsmLink osmLink : amodeusComponent.db.getOsmLinks()) {
                Point p1 = amodeusComponent.getMapPosition(osmLink.getCoordFrom());
                if (p1 != null) {
                    Point p2 = amodeusComponent.getMapPositionAlways(osmLink.getCoordTo());
                    Street street = new Street(osmLink);
                    street.p1 = p1;
                    street.p2 = p2;
                    list.add(street);
                    if (MAX_COUNT < list.size()) {
                        list.clear();
                        break;
                    }
                }
            }
            graphics.setColor(LINKCOLOR);
            for (Street street : list) {
                Point p1 = street.p1;
                Point p2 = street.p2;
                if (p1.equals(p2))
                    graphics.drawOval(p1.x - 1, p1.y - 1, 3, 3);
                else
                    graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
                map.add(street.getLocation(), street);
            }
            count = list.size();
            // ---
            if (drawLabel) {
                final Point point = amodeusComponent.amodeusComponentMouse.getPoint();
                if (Objects.nonNull(point)) {
                    Tensor center = Tensors.vector(point.x, point.y);
                    NdCluster<Street> cluster = map.buildCluster(NdCenterInterface.euclidean(center), 2);
                    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
                    GraphicsUtil.setQualityHigh(graphics);
                    for (NdEntry<Street> entry : cluster.collection()) {
                        Street street = entry.value();
                        double theta = street.angle();
                        Point p1 = street.p1;
                        Point p2 = street.p2;
                        graphics.setColor(Color.RED);
                        graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(p1.x - 1, p1.y - 1, 3, 3);
                        graphics.fillRect(p2.x - 1, p2.y - 1, 3, 3);
                        AffineTransform saveAT = graphics.getTransform();
                        graphics.rotate(theta, p1.x, p1.y);
                        graphics.drawString( //
                                "\u2192 " + street.osmLink.link.getId().toString() + " \u2192", //
                                p1.x + 10, p1.y + 10);
                        graphics.setTransform(saveAT);
                        jTextArea.setText(street.osmLink.link.getId().toString());
                    }
                    GraphicsUtil.setQualityDefault(graphics);
                }
            }
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

    public void setDrawLinks(boolean links) {
        drawLinks = links;
        amodeusComponent.repaint();
    }

    public void setDrawLabel(boolean links) {
        drawLabel = links;
        amodeusComponent.repaint();
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            final JCheckBox jCheckBox = new JCheckBox("streets");
            jCheckBox.setToolTipText("each link as thin line");
            jCheckBox.setSelected(drawLinks);
            jCheckBox.addActionListener(event -> setDrawLinks(jCheckBox.isSelected()));
            rowPanel.add(jCheckBox);
        }
        {
            final JCheckBox jCheckBox = new JCheckBox("click label");
            jCheckBox.setToolTipText("mouse click labels streets with ID");
            jCheckBox.setSelected(drawLabel);
            jCheckBox.addActionListener(event -> setDrawLabel(jCheckBox.isSelected()));
            rowPanel.add(jCheckBox);
        }
        {

            rowPanel.add(jTextArea);
        }
        LazyMouseListener lazyMouseListener = new LazyMouseListener() {
            @Override
            public void lazyClicked(MouseEvent mouseEvent) {
                // System.out.println("here lazy");
                // amodeusComponent.
                // TODO extract lat-log coordinate and store and visualize in component
            }
        };
        LazyMouse lazyMouse = new LazyMouse(lazyMouseListener);
        lazyMouse.addListenersTo(amodeusComponent);
    }
}
