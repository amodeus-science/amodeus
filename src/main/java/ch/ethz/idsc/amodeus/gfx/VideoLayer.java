/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JPanel;

import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.video.VideoGenerator;

/** Head Up Display */
/* package */ class VideoLayer extends ViewerLayer {
    private final File workingDirectory;

    private int fps;
    private int startTime;
    private int endTime;

    public VideoLayer(AmodeusComponent amodeusComponent, File workingDirectory) {
        super(amodeusComponent);
        this.workingDirectory = workingDirectory;
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
                    viewerConfig.save(amodeusComponent, workingDirectory);
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
                (new VideoGenerator(workingDirectory)).start();
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
                SpinnerLabel<Integer> spinnerLabelStart = new SpinnerLabel<>();
                spinnerLabelStart.setStream(IntStream.rangeClosed(0, endTime - 1).boxed());
                spinnerLabelStart.setMenuHover(true);
                spinnerLabelStart.setValueSafe(startTime);
                spinnerLabelStart.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabelStart.getLabelComponent().setToolTipText("video start time [hrs]");

                SpinnerLabel<Integer> spinnerLabelEnd = new SpinnerLabel<>();
                spinnerLabelEnd.setStream(IntStream.rangeClosed(startTime + 1, 30).boxed());
                spinnerLabelEnd.setMenuHover(true);
                spinnerLabelEnd.setValueSafe(endTime);
                spinnerLabelEnd.getLabelComponent().setPreferredSize(new Dimension(55, DEFAULT_HEIGHT));
                spinnerLabelEnd.getLabelComponent().setToolTipText("video end time [hrs]");

                spinnerLabelStart.addSpinnerListener(i -> {
                    startTime = i;
                    spinnerLabelEnd.setStream(IntStream.rangeClosed(startTime + 1, 30).boxed());
                    endTime = Math.max(startTime + 1, endTime);
                    spinnerLabelEnd.setValue(endTime);
                });
                spinnerLabelEnd.addSpinnerListener(i -> {
                    endTime = i;
                    spinnerLabelStart.setStream(IntStream.rangeClosed(0, endTime - 1).boxed());
                    startTime = Math.min(endTime - 1, startTime);
                    spinnerLabelStart.setValue(startTime);
                });

                jPanel.add(spinnerLabelStart.getLabelComponent());
                jPanel.add(spinnerLabelEnd.getLabelComponent());
            }
            rowPanel.add(jPanel);
        }
    }

    @Override
    public void updateSettings(ViewerSettings settings) {
        settings.fps = fps;
        settings.startTime = startTime;
        settings.endTime = Math.max(endTime, settings.startTime + 1);
    }

    @Override
    public void loadSettings(ViewerSettings settings) {
        fps = settings.fps;
        startTime = settings.startTime;
        endTime = settings.endTime;
    }

}
