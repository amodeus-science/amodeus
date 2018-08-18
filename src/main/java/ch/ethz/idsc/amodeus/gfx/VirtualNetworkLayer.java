/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

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

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.view.gheat.gui.ColorSchemes;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;

public class VirtualNetworkLayer extends ViewerLayer {
    public static final Color COLOR = new Color(128, 153 / 2, 0, 128);
    // ---
    private VirtualNetwork<Link> virtualNetwork = null;
    public boolean drawVNodes = true;
    public boolean drawVLinks = false;
    private VirtualNodeGeometry virtualNodeGeometry = null;
    private VirtualNodeShader virtualNodeShader = VirtualNodeShader.None;
    private ColorSchemes colorSchemes = ColorSchemes.Jet;

    public void setVirtualNetwork(VirtualNetwork<Link> virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
        virtualNodeGeometry = new VirtualNodeGeometry(amodeusComponent.db, virtualNetwork);
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (Objects.isNull(virtualNetwork))
            return;

        if (drawVNodes) {
            if (virtualNodeShader.renderBoundary()) {
                graphics.setColor(new Color(128, 128, 128, 128 + 16));
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet())
                    graphics.draw(entry.getValue());
            }

            switch (virtualNodeShader) {
            case None:
                graphics.setColor(new Color(128, 128, 128, 64));
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet())
                    graphics.fill(entry.getValue());
                break;
            case VehicleCount: {
                Tensor count = new VehicleCountVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            case RequestCount: {
                Tensor count = new RequestCountVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    // graphics.setColor(new Color(128, 128, 128, prob.Get(entry.getKey().index).number().intValue()));
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            case MeanRequestDistance: {
                Tensor count = new MeanRequestDistanceVirtualNodeFunction(amodeusComponent.db, virtualNetwork).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    // graphics.setColor(new Color(128, 128, 128, prob.Get(entry.getKey().index).number().intValue()));
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            case MeanRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::meanOrZero).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    // graphics.setColor(new Color(128, 128, 128, prob.Get(entry.getKey().index).number().intValue()));
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            case MedianRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::medianOrZero).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    // graphics.setColor(new Color(128, 128, 128, prob.Get(entry.getKey().index).number().intValue()));
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            case MaxRequestWaiting: {
                Tensor count = new RequestWaitingVirtualNodeFunction( //
                        amodeusComponent.db, virtualNetwork, //
                        StaticHelper::maxOrZero).evaluate(ref);
                Tensor prob = StaticHelper.normalize1Norm(count);
                for (Entry<VirtualNode<Link>, Shape> entry : virtualNodeGeometry.getShapes(amodeusComponent).entrySet()) {
                    // graphics.setColor(new Color(128, 128, 128, prob.Get(entry.getKey().index).number().intValue()));
                    final int i = 255 - prob.Get(entry.getKey().getIndex()).number().intValue();
                    graphics.setColor(halfAlpha(colorSchemes.colorScheme.get(i)));
                    graphics.fill(entry.getValue());
                }
                break;
            }
            default:
                break;
            }
        }
        if (drawVLinks) {
            final MatsimStaticDatabase db = amodeusComponent.db;

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
            SpinnerLabel<VirtualNodeShader> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setToolTipText("virtual node shader");
            spinnerLabel.setArray(VirtualNodeShader.values());
            spinnerLabel.setMenuHover(true);
            spinnerLabel.setValue(virtualNodeShader);
            spinnerLabel.addSpinnerListener(cs -> {
                virtualNodeShader = cs;
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
            spinnerLabel.addSpinnerListener(cs -> {
                colorSchemes = cs;
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
}
