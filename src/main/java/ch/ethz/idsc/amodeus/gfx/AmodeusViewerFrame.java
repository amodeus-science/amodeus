/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.net.DummyStorageSupplier;
import ch.ethz.idsc.amodeus.net.IterationFolder;
import ch.ethz.idsc.amodeus.net.SimulationFolderUtils;
import ch.ethz.idsc.amodeus.net.StorageSupplier;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.view.jmapviewer.Coordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.JMapViewer;
import ch.ethz.idsc.tensor.RealScalar;

/** Demonstrates the usage of {@link JMapViewer} */
public class AmodeusViewerFrame implements Runnable {
    private static final String TITLE = "Amodeus Viewer";
    private static final int MAXDIRECTORYDEPTH = 10;
    private static final String SIMOBJ = "simobj";
    // ---
    private int currentMaxDepth;
    private final AmodeusComponent amodeusComponent;
    private boolean isLaunched = true;
    private final JToggleButton jToggleButtonAuto = new JToggleButton("auto");
    private int playbackSpeed = 50;
    private final Thread thread;
    private StorageUtils storageUtils;

    public final JFrame jFrame = new JFrame();
    private StorageSupplier storageSupplier = new DummyStorageSupplier();

    private final SpinnerLabel<IterationFolder> spinnerLabelIter = new SpinnerLabel<>();
    private final List<SpinnerLabel<String>> spinnerLabelFolderList = new ArrayList<>();
    private final JButton jButtonDecr = new JButton("<");
    private final JButton jButtonIncr = new JButton(">");
    private final SpinnerLabel<Integer> spinnerLabelSpeed = new SpinnerLabel<>();
    private final JSlider jSlider = new JSlider(0, 1, 0);

    /** the new constructor is public AmodeusViewerFrame(AmodeusComponent amodeusComponent, File workingDirectory, File defaultDirectory) */
    public AmodeusViewerFrame(AmodeusComponent amodeusComponent, File outputDirectory) {
        this(amodeusComponent, outputDirectory, outputDirectory);
    }

    /** Constructs the {@code Demo}. */
    public AmodeusViewerFrame(AmodeusComponent amodeusComponent, File workingDirectory, File defaultDirectory) {
        this.amodeusComponent = amodeusComponent;
        // ---
        jFrame.setTitle(TITLE);
        jFrame.setLayout(new BorderLayout());
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isLaunched = false;
                thread.interrupt();
            }
        });
        JPanel panelNorth = new JPanel(new BorderLayout());
        // JPanel panelControls = new JPanel(createFlowLayout());
        JToolBar panelControls = new JToolBar();
        panelControls.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        panelControls.setFloatable(false);
        JToolBar panelConfig = new JToolBar();
        panelConfig.setFloatable(false);
        JPanel panelToprow = new JPanel(new BorderLayout());
        panelToprow.add(panelControls, BorderLayout.CENTER);
        panelToprow.add(panelConfig, BorderLayout.EAST);
        panelNorth.add(panelToprow, BorderLayout.NORTH);
        panelNorth.add(jSlider, BorderLayout.SOUTH);
        jFrame.add(panelNorth, BorderLayout.NORTH);

        JScrollPane jScrollPane;
        {
            RowPanel rowPanel = new RowPanel();
            for (ViewerLayer viewerLayer : amodeusComponent.viewerLayers)
                rowPanel.add(viewerLayer.createPanel());
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(rowPanel.jPanel, BorderLayout.NORTH);
            jScrollPane = new JScrollPane(jPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.setPreferredSize(new Dimension(150, 0));
            jFrame.add(jScrollPane, BorderLayout.EAST);
        }
        jFrame.add(amodeusComponent, BorderLayout.CENTER);
        jFrame.add(amodeusComponent.jLabel, BorderLayout.SOUTH);

        jSlider.addChangeListener(e -> updateFromStorage(jSlider.getValue()));

        AmodeusToggleButton matsimToggleButton = new AmodeusToggleButton(amodeusComponent);
        matsimToggleButton.addActionListener(l -> jSlider.setEnabled(!matsimToggleButton.isSelected()));
        panelControls.add(matsimToggleButton);
        {
            JButton jButton = new JButton("reindex");
            jButton.setToolTipText("reindex available simulation objects");
            jButton.addActionListener(event -> reindex(storageUtils));
            panelControls.add(jButton);
        }

        // find the maximal folder depth of the simulation objects relative to the working directory
        currentMaxDepth = SimulationFolderUtils.getMaxDepth(workingDirectory, SIMOBJ) - 1;
        if (currentMaxDepth < 0) {
            System.out.println("ERROR: no simulation objects found in this working directory!");
            GlobalAssert.that(false);
        }
        if (currentMaxDepth > MAXDIRECTORYDEPTH) {
            System.out.println("ERROR: simulation objects found that are too depth in folder structur!");
            GlobalAssert.that(false);
        }

        for (int i = 0; i < currentMaxDepth; i++) {
            panelControls.addSeparator();
            SpinnerLabel<String> spinnerLabelFolder = new SpinnerLabel<>();
            spinnerLabelFolder.addToComponentReduced(panelControls, new Dimension(60, 26), "folder_" + String.valueOf(i));
            spinnerLabelFolderList.add(spinnerLabelFolder);
        }

        panelControls.addSeparator();
        {
            spinnerLabelIter.addSpinnerListener(i -> {
                storageSupplier = i.storageSupplier();
                jSlider.setMaximum(storageSupplier.size() - 1);
                updateFromStorage(jSlider.getValue());
            });
            spinnerLabelIter.addToComponentReduced(panelControls, new Dimension(50, 26), "iteration");
        }
        panelControls.addSeparator();
        {
            jButtonDecr.addActionListener(e -> jSlider.setValue(jSlider.getValue() - 1));
            panelControls.add(jButtonDecr);
        }
        {
            jButtonIncr.addActionListener(e -> jSlider.setValue(jSlider.getValue() + 1));
            panelControls.add(jButtonIncr);
        }
        {

            spinnerLabelSpeed.setArray( //
                    800, 500, 400, 300, 200, 150, 125, 100, //
                    75, 50, 25, 10, 5, 2, 1);
            spinnerLabelSpeed.setValueSafe(playbackSpeed);
            spinnerLabelSpeed.addSpinnerListener(i -> playbackSpeed = i);
            spinnerLabelSpeed.addToComponentReduced(panelControls, new Dimension(50, 26), "playback factor");
        }
        {
            panelControls.add(jToggleButtonAuto);
        }
        {
            JToggleButton jToggleButton = new JToggleButton("config");
            jToggleButton.setSelected(true);
            jToggleButton.addActionListener(event -> {
                boolean show = jToggleButton.isSelected();
                jScrollPane.setVisible(show);
                amodeusComponent.jLabel.setVisible(show);
                jFrame.validate();
            });
            panelConfig.add(jToggleButton);
        }

        amodeusComponent.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                Point point = mouseEvent.getPoint();
                boolean cursorHand = getJMapViewer().getAttribution().handleAttributionCursor(point);
                amodeusComponent.setCursor(new Cursor(cursorHand //
                        ? Cursor.HAND_CURSOR //
                        : Cursor.DEFAULT_CURSOR));
            }
        });
        updateSubsequentSpinnerLabels(workingDirectory, defaultDirectory, 0);
        thread = new Thread(this);
        thread.start();
    }

    void reindex(StorageUtils storageUtils) {
        // System.out.println("reindex");
        // storageUtils.printStorageProperties();
        List<IterationFolder> list = storageUtils.getAvailableIterations();
        boolean nonEmpty = !list.isEmpty();
        spinnerLabelIter.setEnabled(nonEmpty);
        jButtonDecr.setEnabled(nonEmpty);
        jButtonIncr.setEnabled(nonEmpty);
        spinnerLabelSpeed.setEnabled(nonEmpty);
        jSlider.setEnabled(nonEmpty);
        jToggleButtonAuto.setEnabled(nonEmpty);
        if (!nonEmpty)
            jToggleButtonAuto.setSelected(false);

        if (nonEmpty) {
            spinnerLabelIter.setList(list);
            IterationFolder last = list.get(list.size() - 1);
            storageSupplier = last.storageSupplier();
            spinnerLabelIter.setValueSafe(last);
            jSlider.setMaximum(storageSupplier.size() - 1);
        }
    }

    private void updateSubsequentSpinnerLabels(File rootDirectory, File defaultDirectory, int listIndex) {
        if (listIndex >= currentMaxDepth)
            return;
        SpinnerLabel<String> spinnerLabelFolder = spinnerLabelFolderList.get(listIndex);

        File[] subfolders = MultiFileTools.getAllDirectoriesSortedWithSubfolderName(rootDirectory, SIMOBJ);

        spinnerLabelFolder.setArray(Stream.of(subfolders).map(v -> v.getName()).toArray(String[]::new));
        spinnerLabelFolder.setIndex(0);
        spinnerLabelFolder.setSpinnerListener(s -> {
            File selectedFolder = new File(rootDirectory, s);

            setSpinnerLabel(selectedFolder, null, listIndex);
            spinnerLabelFolder.updateLabel();
        });

        // set default value else just take the first folder
        int index = 0;
        if (defaultDirectory != null) {
            index = getDefaultFolderIndex(rootDirectory, defaultDirectory);
        }

        spinnerLabelFolder.setIndex(index);
        setSpinnerLabel(subfolders[index], defaultDirectory, listIndex);
        spinnerLabelFolder.updateLabel();
    }

    private int getDefaultFolderIndex(File rootDirectory, File defaultDirectory) {
        if (rootDirectory.equals(defaultDirectory))
            return 0;
        File rootChild = defaultDirectory;
        while (!rootDirectory.equals(rootChild.getParentFile())) {
            rootChild = rootChild.getParentFile();
        }
        List<File> subfolders = Arrays.asList(MultiFileTools.getAllDirectoriesSortedWithSubfolderName(rootDirectory, SIMOBJ));
        return subfolders.indexOf(rootChild);
    }

    private void setSpinnerLabel(File selectedFolder, File defaultDirectory, int index) {
        if (MultiFileTools.containsFolderName(selectedFolder, SIMOBJ)) {
            this.storageUtils = new StorageUtils(selectedFolder);
            reindex(storageUtils);
            removeSubsequentSpinnerLabels(index + 1);
        } else {
            updateSubsequentSpinnerLabels(selectedFolder, defaultDirectory, index + 1);
        }
    }

    private void removeSubsequentSpinnerLabels(int listIndex) {
        if (listIndex >= currentMaxDepth)
            return;
        SpinnerLabel<String> spinnerLabelFolder = spinnerLabelFolderList.get(listIndex);

        spinnerLabelFolder.setArray("");
        spinnerLabelFolder.removeSpinnerListeners();
        removeSubsequentSpinnerLabels(listIndex + 1);
        spinnerLabelFolder.updateLabel();
    }

    void updateFromStorage(int index) {
        try {
            amodeusComponent.setSimulationObject(storageSupplier.getSimulationObject(index));
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("cannot load: " + index);
        }
    }

    private JMapViewer getJMapViewer() {
        return amodeusComponent;
    }

    public void setDisplayPosition(Coord coord, int zoom) {
        setDisplayPosition(coord.getY(), coord.getX(), zoom);
    }

    public void setDisplayPosition(double lat, double lon, int zoom) {
        getJMapViewer().setDisplayPosition(new Point(), new Coordinate(lat, lon), zoom);
    }

    @Override
    public void run() {
        while (isLaunched) {
            int millis = 500;
            if (jSlider != null && jToggleButtonAuto.isSelected()) {
                jSlider.setValue(jSlider.getValue() + 1);
                int STEPSIZE_SECONDS = storageSupplier.getIntervalEstimate();
                millis = RealScalar.of(1000 * STEPSIZE_SECONDS).divide(RealScalar.of(playbackSpeed)).number().intValue();
            }
            try {
                Thread.sleep(millis);
            } catch (Exception e) {
                // ---
            }
        }
    }

}
