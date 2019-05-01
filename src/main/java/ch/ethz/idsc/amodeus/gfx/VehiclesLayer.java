/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;

import org.matsim.contrib.dvrp.data.Vehicles;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.gui.RowPanel;
import ch.ethz.idsc.amodeus.util.gui.SpinnerLabel;

public class VehiclesLayer extends ViewerLayer {

    private final BitSet bits = new BitSet();

    // during development standard colors are a better default
    public RoboTaxiStatusColors statusColors;
    public boolean showLocation;

<<<<<<< HEAD
    public VehiclesLayer() {
//        bitSet(RoboTaxiStatus.DRIVETOCUSTOMER);
        bitSet(RoboTaxiStatus.REBALANCEDRIVE);
=======
    public VehiclesLayer(AmodeusComponent amodeusComponent) {
        super(amodeusComponent);
        loadBitSet(amodeusComponent.defaultConfig.settings);
>>>>>>> master
    }

    @Override
    protected void paint(Graphics2D graphics, SimulationObject ref) {
        if (ref == null || !showLocation)
            return;

        int zoom = amodeusComponent.getZoom();
        int carwidth = (int) Math.max(zoom <= 12 ? 2 : 3, Math.round(5 / amodeusComponent.getMeterPerPixel()));
        int car_half = carwidth / 2;
        Map<Integer, List<VehicleContainer>> map = //
                ref.vehicles.stream().collect(Collectors.groupingBy(VehicleContainer::getLinkId));
        for (Entry<Integer, List<VehicleContainer>> entry : map.entrySet()) {
            int size = entry.getValue().size();
            OsmLink osmLink = amodeusComponent.db.getOsmLink(entry.getKey());
            Point p1test = amodeusComponent.getMapPosition(osmLink.getAt(0.5));
            if (p1test != null) {
                double ofs = 0.5 / size;
                double delta = 2 * ofs;
                for (VehicleContainer vc : entry.getValue()) {
                    Point p1 = amodeusComponent.getMapPosition(osmLink.getAt(ofs));
                    if (p1 != null) {
                        if (showLocation) {
                            Color color = statusColors.of(vc.roboTaxiStatus);
                            graphics.setColor(color);
                            graphics.fillRect(p1.x - car_half, p1.y - car_half, carwidth, carwidth);
                        }
                        if (bits.get(vc.roboTaxiStatus.ordinal())) {
                            OsmLink toOsmLink = amodeusComponent.db.getOsmLink(vc.destinationLinkIndex);
                            Point p2 = amodeusComponent.getMapPositionAlways(toOsmLink.getAt(0.5));
                            Color col = statusColors.ofDest(vc.roboTaxiStatus);
                            graphics.setColor(col);
                            graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
                        }
                    }
                    ofs += delta;
                }
            }
        }
    }

    @Override
    protected void hud(Graphics2D graphics, SimulationObject ref) {
        int[] count = new int[RoboTaxiStatus.values().length];
        if (ref != null) {
            ref.vehicles.forEach(v -> ++count[v.roboTaxiStatus.ordinal()]);
            
            for (RoboTaxiStatus avStatus : RoboTaxiStatus.values()) {
                InfoString infoString = new InfoString(String.format("%5d %s", count[avStatus.ordinal()], avStatus.description() + " RoboTaxi"));
                infoString.color = statusColors.of(avStatus);
                if(avStatus == RoboTaxiStatus.OFFSERVICE) {
                	continue;
                }
                amodeusComponent.append(infoString);
            }
            InfoString infoString = new InfoString(String.format("%5d %s", ref.vehicles.size(), "total number of RoboTaxis"));
            infoString.color = Color.BLACK;
            amodeusComponent.append(infoString);
            amodeusComponent.appendSeparator();
            
            InfoString infoStringDestLine = new InfoString(String.format("%s","  - destination line"));
            infoStringDestLine.color = statusColors.of(RoboTaxiStatus.REBALANCEDRIVE);
            amodeusComponent.append(infoStringDestLine);
            amodeusComponent.appendSeparator();
        }
    }

    @Override
    protected void createPanel(RowPanel rowPanel) {
        {
            final JCheckBox jCheckBox = new JCheckBox("location");
            jCheckBox.setToolTipText("vehicle are small rectangles");
            jCheckBox.setSelected(showLocation);
            jCheckBox.addActionListener(event -> {
                showLocation = jCheckBox.isSelected();
                amodeusComponent.repaint();
            });
            rowPanel.add(jCheckBox);
        }
        {
            SpinnerLabel<RoboTaxiStatusColors> spinner = new SpinnerLabel<>();
            spinner.setToolTipText("color scheme for vehicle rectangles");
            spinner.setArray(RoboTaxiStatusColors.values());
            spinner.setValue(statusColors);
            spinner.addSpinnerListener(cs -> {
                statusColors = cs;
                amodeusComponent.repaint();
            });
            spinner.getLabelComponent().setPreferredSize(new Dimension(100, DEFAULT_HEIGHT));
            rowPanel.add(spinner.getLabelComponent());
        }

        for (RoboTaxiStatus status : RoboTaxiStatus.values())
            if (status.isDriving()) {
                final JCheckBox jCheckBox = new JCheckBox(status.description());
                jCheckBox.setToolTipText("show vehicles in mode: " + status.description());
                jCheckBox.setSelected(bits.get(status.ordinal()));
                jCheckBox.addActionListener(e -> setDrawDestinations(status, jCheckBox.isSelected()));
                rowPanel.add(jCheckBox);
            }
    }

    public void setDrawDestinations(RoboTaxiStatus status, boolean selected) {
        bits.set(status.ordinal(), selected);
        amodeusComponent.repaint();
    }

    private void bitSet(RoboTaxiStatus roboTaxiStatus) {
        if (roboTaxiStatus.isDriving())
            bits.set(roboTaxiStatus.ordinal());
        else
            System.err.println("cannot visualize dest link");
    }

    @Override
    public void updateSettings(ViewerSettings settings) {
        settings.bits = bits;
        settings.statusColors = statusColors;
        settings.showLocation = showLocation;
    }

    @Override
    public void loadSettings(ViewerSettings settings) {
        try {
            loadBitSet(settings);
        } catch (NullPointerException e) {
            // ---
        }
        statusColors = settings.statusColors;
        showLocation = settings.showLocation;
    }

    private void loadBitSet(ViewerSettings settings) {
        if (settings.bits == null) {
            bitSet(RoboTaxiStatus.DRIVETOCUSTOMER);
            bitSet(RoboTaxiStatus.REBALANCEDRIVE);
            settings.bits = bits;
        } else {
            bits.clear();
            settings.bits.stream().forEach(bits::set);
        }
    }
}
