/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.JCheckBox;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.SimulationObject;
import amodeus.amodeus.net.TensorCoords;
import amodeus.amodeus.util.gui.RowPanel;
import amodeus.amodeus.util.gui.SpinnerLabel;
import amodeus.amodeus.view.gheat.gui.ColorSchemes;
import amodeus.amodeus.virtualnetwork.core.VirtualLink;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.opt.ConvexHull;
import ch.ethz.idsc.tensor.sca.Sqrt;

public class VirtualNetworkLayer extends ViewerLayer {
    private static final Scalar _224 = RealScalar.of(224);
    public static final Color COLOR = new Color(128, 153 / 2, 0, 128);
    // ---
    private VirtualNetwork<Link> virtualNetwork = null;
    private VirtualNodeGeometry virtualNodeGeometry = null;

    public boolean drawVNodes;
    public boolean drawVLinks;
    public VirtualNodePrescale virtualNodePrescale = VirtualNodePrescale.IDENTITY;
    public VirtualNodeShader virtualNodeShader;
    public Rescaling rescaling;
    public ColorSchemes colorSchemes;

    public VirtualNetworkLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
        amodeusComponent.virtualNetworkLayer = this;
    }

    public void setVirtualNetwork(VirtualNetwork<Link> virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
        virtualNodeGeometry = new VirtualNodeGeometry(amodeusComponent.db, virtualNetwork);
    }

    public Tensor prescale(Tensor count) {
        switch (virtualNodePrescale) {
        case IDENTITY:
            return count;
        case ROOT_AREA:
            return count.pmul(Sqrt.of(virtualNodeGeometry.inverseArea()));
        case AREA:
            return count.pmul(virtualNodeGeometry.inverseArea());
        }
        throw new UnsupportedOperationException();
    }

    private static Color withAlpha(Color myColor, int alpha) {
        return new Color(myColor.getRed(), myColor.getGreen(), myColor.getBlue(), alpha);
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (Objects.isNull(virtualNetwork))
            return;

        if (drawVNodes) {
            final Tensor center = Tensors.vectorInt( //
                    amodeusComponent.getWidth() / 2, //
                    amodeusComponent.getHeight());
            // {
            // graphics.setColor(new Color(128, 128, 128, 128));
            // for (Shape shape : virtualNodeGeometry.getShapes(amodeusComponent).values())
            // graphics.draw(shape);
            //
            // }

            switch (virtualNodeShader) {
            case None:
                graphics.setColor(new Color(128, 128, 128, 16));
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet())
                    graphics.fill(entry.getValue());
                // graphics.setColor(new Color(128, 128, 128, 64));
                // for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet())
                // graphics.draw(entry.getValue());
                break;
            case VehicleCount: {
                Tensor count = prescale(new VehicleCountVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref));
                Tensor prep = rescaling.apply(count);
                Tensor prob = prep.multiply(_224);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    entry.getKey(); // <- eliminates warning
                    // final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    // graphics.setColor(halfAlpha(colorSchemes.colorDataIndexed.getColor(i)));
                    // graphics.fill(entry.getValue());
                }

                {

                    for (Entry<VirtualNode<Link>, Tensor> entry : virtualNodeGeometry.getShapesTensor(amodeusComponent).entrySet()) {
                        Scalar scalar = prep.Get(entry.getKey().getIndex()).multiply(RealScalar.of(0.3));
                        Tensor hull = entry.getValue();
                        Tensor tensor = Tensors.reserve(hull.length());
                        for (Tensor pnt : hull) {
                            Tensor dif = pnt.subtract(center).multiply(scalar);
                            Tensor sca = pnt.add(dif);
                            tensor.append(sca);
                        }

                        {
                            Tensor convexHull = ConvexHull.of(Join.of(hull, tensor));
                            Shape shape = VirtualNodeGeometry.createShapePixel(amodeusComponent, convexHull);
                            final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                            Color color2 = colorSchemes.colorDataIndexed.getColor(i);
                            // graphics.setColor(halfAlpha(colorSchemes.colorDataIndexed.getColor(i)));

                            graphics.setColor(withAlpha(color2, 64));
                            graphics.fill(shape);
                        }
                        {
                            Shape shape = VirtualNodeGeometry.createShapePixel(amodeusComponent, tensor);
                            graphics.setColor(new Color(255, 255, 255, 128));
                            graphics.draw(shape);
                        }
                    }

                }

                break;
            }
            case RequestCount: {
                Tensor count = prescale(new RequestCountVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref));
                probFill(graphics, count);
                break;
            }
            case MeanRequestDistance: {
                Tensor count = new MeanRequestDistanceVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref);
                probFill(graphics, count);
                break;
            }
            case MeanRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::meanOrZero).evaluate(ref);
                probFill(graphics, count);
                break;
            }
            case MedianRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::medianOrZero).evaluate(ref);
                probFill(graphics, count);
                break;
            }
            case MaxRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::maxOrZero).evaluate(ref);
                probFill(graphics, count);
                break;
            }
            default:
                break;
            }
        }
        if (drawVLinks) {
            final MatsimAmodeusDatabase db = amodeusComponent.db;

            graphics.setColor(new Color(255, 0, 0, 128));
            graphics.setStroke(new BasicStroke(3));
            for (VirtualLink<Link> vl : virtualNetwork.getVirtualLinks()) {
                VirtualNode<Link> n1 = vl.getFrom();
                VirtualNode<Link> n2 = vl.getTo();
                Coord c1 = db.referenceFrame.coords_toWGS84().transform(TensorCoords.toCoord(n1.getCoord()));
                Coord c2 = db.referenceFrame.coords_toWGS84().transform(TensorCoords.toCoord(n2.getCoord()));
                Point p1 = amodeusComponent.getMapPositionAlways(c1);
                Point p2 = amodeusComponent.getMapPositionAlways(c2);
                Shape shape = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
                graphics.draw(shape);
                // graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            graphics.setStroke(new BasicStroke(1));
        }
    }

    private void probFill(Graphics2D graphics, Tensor count) {
        Tensor prob = rescaling.apply(count).multiply(_224);
        virtualNodeGeometry.getShapes(amodeusComponent).forEach((key, value) -> {
            // graphics.setColor(new Color(128, 128, 128, prob.Get(key.index).number().intValue()));
            final int i = 255 - prob.Get(key.getIndex()).number().intValue();
            graphics.setColor(halfAlpha(colorSchemes.colorDataIndexed.getColor(i)));
            graphics.fill(value);
        });
    }

    private void setDrawVNodes(boolean selected) {
        drawVNodes = selected;
        amodeusComponent.repaint();
    }

    private void setDrawVLinks(boolean selected) {
        drawVLinks = selected;
        amodeusComponent.repaint();
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            final JCheckBox jCheckBox = new JCheckBox("nodes");
            jCheckBox.setToolTipText("voronoi boundaries of virtual nodes");
            jCheckBox.setSelected(drawVNodes);
            jCheckBox.addActionListener(e -> setDrawVNodes(jCheckBox.isSelected()));
            rowPanel.add(jCheckBox);
        }
        {
            SpinnerLabel<VirtualNodePrescale> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("virtual node prescaler");
            spinnerLabel.setArray(VirtualNodePrescale.values());
            spinnerLabel.setMenuHover(true);
            spinnerLabel.setValue(virtualNodePrescale);
            spinnerLabel.addSpinnerListener(value -> {
                virtualNodePrescale = value;
                amodeusComponent.repaint();
            });
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            rowPanel.add(spinnerLabel.getLabelComponent());
        }
        {
            SpinnerLabel<VirtualNodeShader> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("virtual node shader");
            spinnerLabel.setArray(VirtualNodeShader.values());
            spinnerLabel.setMenuHover(true);
            spinnerLabel.setValue(virtualNodeShader);
            spinnerLabel.addSpinnerListener(value -> {
                virtualNodeShader = value;
                amodeusComponent.repaint();
            });
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            rowPanel.add(spinnerLabel.getLabelComponent());
        }
        {
            SpinnerLabel<Rescaling> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("virtual node rescale");
            spinnerLabel.setArray(Rescaling.values());
            spinnerLabel.setMenuHover(true);
            spinnerLabel.setValue(rescaling);
            spinnerLabel.addSpinnerListener(value -> {
                rescaling = value;
                amodeusComponent.repaint();
            });
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            rowPanel.add(spinnerLabel.getLabelComponent());
        }
        {
            SpinnerLabel<ColorSchemes> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("color scheme");
            spinnerLabel.setArray(ColorSchemes.values());
            spinnerLabel.setValue(colorSchemes);
            spinnerLabel.addSpinnerListener(value -> {
                colorSchemes = value;
                amodeusComponent.repaint();
            });
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            spinnerLabel.setMenuHover(true);
            rowPanel.add(spinnerLabel.getLabelComponent());
        }

        {
            final JCheckBox jCheckBox = new JCheckBox("links");
            jCheckBox.setToolTipText("virtual links between nodes");
            jCheckBox.setSelected(drawVLinks);
            jCheckBox.addActionListener(e -> setDrawVLinks(jCheckBox.isSelected()));
            rowPanel.add(jCheckBox);
        }
    }

    private static Color halfAlpha(Color color) {
        int rgb = color.getRGB() & 0xffffff;
        int alpha = color.getAlpha() / 2;
        return new Color(rgb | (alpha << 24), true);
    }

    @Override
    public void updateSettings(ViewerSettings viewerSettings) {
        viewerSettings.drawVNodes = drawVNodes;
        viewerSettings.drawVLinks = drawVLinks;
        // TODO @datahaki resolve or add improved explanation prescaler
        viewerSettings.virtualNodeShader = virtualNodeShader;
        viewerSettings.rescaling = rescaling;
        viewerSettings.colorSchemes = colorSchemes;
    }

    @Override
    public void loadSettings(ViewerSettings viewerSettings) {
        drawVNodes = viewerSettings.drawVNodes;
        drawVLinks = viewerSettings.drawVLinks;
        // TODO @datahaki resolve or add improved explanation prescaler
        virtualNodeShader = viewerSettings.virtualNodeShader;
        rescaling = viewerSettings.rescaling;
        colorSchemes = viewerSettings.colorSchemes;
    }
}
