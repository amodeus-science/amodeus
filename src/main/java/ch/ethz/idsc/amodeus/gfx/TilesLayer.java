/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;

public class TilesLayer extends ViewerLayer {

    static enum Blend {
        Dark(0), Light(255);
        final int rgb;

        private Blend(int rgb) {
            this.rgb = rgb;
        }
    }

    public TilesLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            SpinnerLabel<TileSource> sl = JMapTileSelector.create(amodeusComponent);
            rowPanel.add(sl.getLabelComponent());
        }
        {
            JPanel jPanel = new JPanel(new FlowLayout(1, 2, 2));
            {
                SpinnerLabel<Blend> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setArray(Blend.values());
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(amodeusComponent.mapGrayCover == 255 ? Blend.Light : Blend.Dark);
                spinnerLabel.addSpinnerListener(i -> {
                    amodeusComponent.mapGrayCover = i.rgb;
                    amodeusComponent.repaint();
                });
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("cover gray level");
                jPanel.add(spinnerLabel.getLabelComponent());

            }
            {
                SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setArray(0, 32, 64, 96, 128, 160, 192, 224, 255);
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(amodeusComponent.mapAlphaCover);
                spinnerLabel.addSpinnerListener(i -> amodeusComponent.setMapAlphaCover(i));
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("cover alpha");
                jPanel.add(spinnerLabel.getLabelComponent());

            }
            rowPanel.add(jPanel);
        }
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        // nothing to do here
    }

    public void updateSettings(ViewerSettings settings) {
        settings.mapAlphaCover = amodeusComponent.mapAlphaCover;
        settings.mapGrayCover = amodeusComponent.mapGrayCover;
        settings.tileSourceName = amodeusComponent.getTileSource().getName();
    }

    public void loadSettings(ViewerSettings settings) {
        amodeusComponent.mapAlphaCover = settings.mapAlphaCover;
        amodeusComponent.mapGrayCover = settings.mapGrayCover;
        amodeusComponent.setTileSource(MapSource.valueOf(settings.tileSourceName).getTileSource());
    }

}
