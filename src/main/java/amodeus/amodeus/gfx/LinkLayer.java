/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import amodeus.amodeus.net.OsmLink;
import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.util.gui.GraphicsUtil;
import amodeus.amodeus.util.gui.LazyMouse;
import amodeus.amodeus.util.gui.LazyMouseListener;
import amodeus.amodeus.util.gui.RowPanel;
import amodeus.amodeus.util.math.Scalar2Number;
import amodeus.amodeus.view.jmapviewer.interfaces.ICoordinate;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.opt.nd.EuclideanNdCenter;
import ch.ethz.idsc.tensor.opt.nd.NdMap;
import ch.ethz.idsc.tensor.opt.nd.NdMatch;
import ch.ethz.idsc.tensor.opt.nd.NdTreeMap;
import ch.ethz.idsc.tensor.sca.ArcTan;

/* package */ class Street {
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
        return Scalar2Number.of(ArcTan.of(p2.x - p1.x, p2.y - p1.y)).doubleValue();
    }
}

public class LinkLayer extends ViewerLayer {
    private static final Color LINKCOLOR = new Color(153, 153, 102, 64);
    // ---
    public int linkLimit = 8192;
    private boolean drawLinks;
    private boolean drawLabel;
    private boolean drawCoordinates;
    private final JTextArea jTextArea = new JTextArea(2, 10);
    private int count = 0;
    private ICoordinate lastCoord;

    public LinkLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (drawLinks) {
            Dimension dimension = amodeusComponent.getSize();
            NdMap<Street> map = new NdTreeMap<>(Array.zeros(2), Tensors.vector(dimension.width, dimension.height), 12, 8);
            // draw links
            drawLinks(map, graphics);
            // draw labels
            if (drawLabel && Objects.nonNull(lastCoord))
                drawLabel(map, graphics);
            else
                jTextArea.setText("");
        }
        // draw location where mouse clicked
        if (drawCoordinates && Objects.nonNull(lastCoord))
            drawLastCoord(graphics);
    }

    /** Draw last coordinate of where mouse clicked onto graphics object
     *
     * @param graphics Graphics2D object on which the last coord will be
     *            drawn/highlighted */
    private void drawLastCoord(Graphics2D graphics) {
        Point p = amodeusComponent.getMapPosition(lastCoord.getLat(), lastCoord.getLon());
        if (Objects.nonNull(p)) {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(p.x - 1, p.y - 1, 3, 3);
            graphics.drawString(String.format("[lng=%.6f, lat=%.6f]", lastCoord.getLon(), lastCoord.getLat()), p.x + 10, p.y - 10);
        }
    }

    /** Draw links onto graphics object
     *
     * @param map to find closest streets
     * @param graphics Graphics2D object on which the links are drawn */
    private void drawLinks(NdMap<Street> map, Graphics2D graphics) {
        List<Street> list = new LinkedList<>();
        for (OsmLink osmLink : amodeusComponent.db.getOsmLinks()) {
            Point p1 = amodeusComponent.getMapPosition(osmLink.getCoordFrom());
            if (Objects.nonNull(p1)) {
                Point p2 = amodeusComponent.getMapPositionAlways(osmLink.getCoordTo());
                Street street = new Street(osmLink);
                street.p1 = p1;
                street.p2 = p2;
                list.add(street);
                if (linkLimit < list.size()) {
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
    }

    /** Draw labels onto graphics object
     *
     * @param map to find closest streets
     * @param graphics Graphics2D object on which the labels are to be drawn */
    private void drawLabel(NdMap<Street> map, Graphics2D graphics) {
        final Point point = amodeusComponent.getMapPosition(lastCoord.getLat(), lastCoord.getLon());
        if (Objects.nonNull(point)) {
            Tensor center = Tensors.vector(point.x, point.y);
            Collection<NdMatch<Street>> cluster = map.cluster(EuclideanNdCenter.of(center), 2);
            graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
            GraphicsUtil.setQualityHigh(graphics);
            for (NdMatch<Street> ndMatch : cluster) {
                Street street = ndMatch.value();
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

    public void setDrawLabel(boolean label) {
        drawLabel = label;
        amodeusComponent.repaint();
    }

    public void setDrawCoordinates(boolean coords) {
        drawCoordinates = coords;
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
        {
            final JCheckBox jCheckBox = new JCheckBox("click coordinates");
            jCheckBox.setToolTipText("longitude and latitude of click position");
            jCheckBox.setSelected(drawCoordinates);
            jCheckBox.addActionListener(event -> setDrawCoordinates(jCheckBox.isSelected()));
            rowPanel.add(jCheckBox);
        }
        LazyMouseListener lazyMouseListener = new LazyMouseListener() {
            @Override
            public void lazyClicked(MouseEvent mouseEvent) {
                // set last coordinate
                lastCoord = amodeusComponent.getPosition(mouseEvent.getPoint());
            }
        };
        LazyMouse lazyMouse = new LazyMouse(lazyMouseListener);
        lazyMouse.addListenersTo(amodeusComponent);
    }

    @Override
    public void updateSettings(ViewerSettings settings) {
        settings.drawLinks = drawLinks;
        settings.drawLabel = drawLabel;
        settings.drawCoordinates = drawCoordinates;
    }

    @Override
    public void loadSettings(ViewerSettings settings) {
        setDrawLinks(settings.drawLinks);
        setDrawLabel(settings.drawLabel);
        setDrawCoordinates(settings.drawCoordinates);
    }
}
