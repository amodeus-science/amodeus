/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.net.*;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.video.VideoGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Head Up Display */
public class VideoLayer extends ViewerLayer {

    private int fps;
    private int startTime;
    private int endTime;

    public VideoLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
    }

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
                try {
                    ViewerConfig viewerConfig = ViewerConfig.fromDefaults(amodeusComponent.db);
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
                try {
                    (new VideoGenerator(MultiFileTools.getWorkingDirectory())).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            rowPanel.add(jButton);
        }
        {
            SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
            spinnerLabel.setStream(IntStream.rangeClosed(10, 100).boxed().filter(i -> i % 5 == 0));
            spinnerLabel.setMenuHover(true);
            spinnerLabel.setValueSafe(fps);
            spinnerLabel.addSpinnerListener(i -> fps = i);
            spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
            spinnerLabel.getLabelComponent().setToolTipText("frames per second");
            rowPanel.add(spinnerLabel.getLabelComponent());
        }
        {
            JPanel jPanel = new JPanel(new FlowLayout(1, 2, 2));
            {
                SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setStream(IntStream.rangeClosed(0, endTime - 1).boxed());
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(startTime);
                spinnerLabel.addSpinnerListener(i -> startTime = i);
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("video start time [hrs]");
                jPanel.add(spinnerLabel.getLabelComponent());

            }
            {
                SpinnerLabel<Integer> spinnerLabel = new SpinnerLabel<>();
                spinnerLabel.setStream(IntStream.rangeClosed(1, 30).boxed());
                spinnerLabel.setMenuHover(true);
                spinnerLabel.setValueSafe(endTime);
                spinnerLabel.addSpinnerListener(i -> endTime = Math.max(i, startTime + 1));
                spinnerLabel.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabel.getLabelComponent().setToolTipText("video end time [hrs]");
                jPanel.add(spinnerLabel.getLabelComponent());

            }
            rowPanel.add(jPanel);
        }
    }

    public void updateSettings(ViewerSettings settings) {
        settings.fps = fps;
        settings.startTime = startTime;
        settings.endTime = Math.max(endTime, settings.startTime + 1);
    }

    public void loadSettings(ViewerSettings settings) {
        fps = settings.fps;
        startTime = settings.startTime;
        endTime = settings.endTime;
    }

}
