/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/** Head Up Display */
public class VideoLayer extends ViewerLayer {

    public boolean show = true;

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        // ---
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            JButton jButton = new JButton("export");
            jButton.setToolTipText("export viewer settings to file in working directory");
            jButton.addActionListener(event -> {
                ViewerConfig viewerConfig = ViewerConfig.fromDefaults(amodeusComponent.db);
                try {
                    viewerConfig.save(amodeusComponent, MultiFileTools.getWorkingDirectory());
                    System.out.println(viewerConfig);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            rowPanel.add(jButton);
        }
        {
            JButton jButton = new JButton("record");
            jButton.setToolTipText("record video with settings found in working directory or defaults");
            jButton.addActionListener(event -> {
                // TODO perform some action
                // implement video recorder in amodeus
            });
            rowPanel.add(jButton);
        }

        SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
        spinnerLabel.setArray(0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24);
        spinnerLabel.setMenuHover(true);
        spinnerLabel.setValueSafe(amodeusComponent.getFontSize());
        /* spinnerLabel.addSpinnerListener(i -> {
            amodeusComponent.setFontSize(i);
            amodeusComponent.repaint();
        }); */
        spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
        spinnerLabel.getLabelComponent().setToolTipText("font size of info");
        rowPanel.add(spinnerLabel.getLabelComponent());

    }

}
