/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.GraphicsUtil;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.util.math.LruCache;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.img.Hue;
import ch.ethz.idsc.tensor.red.Total;

public class LoadLayer extends ViewerLayer {
    private static final int DEFAULT_INTERVAL = 10;
    private static final int HISTORY_MAX = 10;

    private final Map<Long, SimulationObject> lruCache = LruCache.create(HISTORY_MAX);
    public volatile boolean drawLoad;
    public volatile int historyLength;
    public volatile int loadScale;

    public LoadLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (Objects.isNull(ref))
            return;

        lruCache.put(ref.now, ref);

        if (drawLoad) {
            final int width = historyLength;
            LoadStats linkStats = new LoadStats(width);
            linkStats.feed(ref, 0);
            int DT = DEFAULT_INTERVAL;
            for (int ofs = 1; ofs < width; ++ofs)
                if (lruCache.containsKey(ref.now - DT * ofs))
                    linkStats.feed(lruCache.get(ref.now - DT * ofs), ofs);

            Tensor weight;
            if (1 < width) {
                weight = Subdivide.of(RealScalar.of(1), RealScalar.of(.1), width - 1);
                weight = weight.divide(Total.of(weight).Get());
            } else
                weight = Tensors.of(RealScalar.ONE);

            GraphicsUtil.setQualityHigh(graphics);
            // System.out.println(linkStats.linkIndex.size());
            double scaling = loadScale * 1000;
            for (Entry<Integer, Tensor> entry : linkStats.linkTensor.entrySet()) {
                final int index = entry.getKey();
                final OsmLink osmLink = amodeusComponent.db.getOsmLink(index);
                final double factor = Math.max(osmLink.getLength(), 1) * amodeusComponent.getMeterPerPixel();
                Point p1 = amodeusComponent.getMapPosition(osmLink.getCoordFrom());
                if (Objects.nonNull(p1)) {
                    Point p2 = amodeusComponent.getMapPositionAlways(osmLink.getCoordTo());
                    Tensor linkTable = weight.dot(entry.getValue());
                    final double total = Total.of(linkTable).Get().number().doubleValue();
                    final double carsEmpty = linkTable.Get(1).number().doubleValue();
                    double ratio = carsEmpty / total;
                    double h = (ratio + 0.8) / 3; // r=0->Green, r=1->Blue
                    double v = 0.84 + ratio * .15; // r=0->, r=1->Brighter
                    graphics.setColor(Hue.of(h, 1, v, .75));
                    Stroke stroke = new BasicStroke((float) Math.sqrt(scaling * total / factor));
                    graphics.setStroke(stroke);
                    Shape shape = new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
                    graphics.draw(shape);
                }
            }
            GraphicsUtil.setQualityDefault(graphics);
            graphics.setStroke(new BasicStroke());
        }
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            final JCheckBox jCheckBox = new JCheckBox("load");
            jCheckBox.setToolTipText("width proportional to number of vehicles on link");
            jCheckBox.setSelected(drawLoad);
            jCheckBox.addActionListener(event -> {
                drawLoad = jCheckBox.isSelected();
                amodeusComponent.repaint();
            });
            rowPanel.add(jCheckBox);
        }
        {
            JPanel jPanel = new JPanel(new FlowLayout(1, 2, 2));
            {
                SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setArray(1, 2, 3, 4, 5);
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(historyLength);
                spinnerLabel.addSpinnerListener(i -> {
                    historyLength = i;
                    amodeusComponent.repaint();
                });
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("load history length");
                jPanel.add(spinnerLabel.getLabelComponent());

            }
            {
                SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(loadScale);
                spinnerLabel.addSpinnerListener(i -> {
                    loadScale = i;
                    amodeusComponent.repaint();
                });
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("load scaling");
                jPanel.add(spinnerLabel.getLabelComponent());
            }
            rowPanel.add(jPanel);
        }
    }

    @Override
    public void updateSettings(ViewerSettings settings) {
        settings.drawLoad = drawLoad;
        settings.historyLength = historyLength;
        settings.loadScale = loadScale;
    }

    @Override
    public void loadSettings(ViewerSettings settings) {
        drawLoad = settings.drawLoad;
        historyLength = settings.historyLength;
        loadScale = settings.loadScale;
    }
}
