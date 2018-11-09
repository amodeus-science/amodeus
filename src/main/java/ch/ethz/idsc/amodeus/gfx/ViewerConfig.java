package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.ICoordinate;
import org.matsim.api.core.v01.Coord;

import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ViewerConfig {
    private static String defaultFileName = "viewerSettings";
    public ViewerSettings settings;

    private ViewerConfig(MatsimAmodeusDatabase db, ViewerSettings settings) {
        this.settings = settings;
        if (this.settings.coord == null) {
            this.settings.coord = db.getCenter();
        }
    }

    private ViewerConfig(MatsimAmodeusDatabase db) {
        settings = new ViewerSettings();
        settings.coord = db.getCenter();
    }

    public static ViewerConfig fromDefaults(MatsimAmodeusDatabase db) {
        return new ViewerConfig(db);
    }

    public static ViewerConfig from(AmodeusComponent amodeusComponent) {
        return new ViewerConfig(amodeusComponent.db).update(amodeusComponent);
    }

    public static ViewerConfig from(MatsimAmodeusDatabase db, File workingDirectory) throws IOException {
        File settingsFile = new File(workingDirectory, defaultFileName);
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(settingsFile));
            ViewerSettings settings = (ViewerSettings) stream.readObject();
            stream.close();
            return new ViewerConfig(db, settings);
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Unable to find file: %s! Continue with default setting.", //
                    settingsFile.getAbsolutePath()));
            return ViewerConfig.fromDefaults(db);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":\n" + Stream.of(settings.getClass().getFields()).map(f -> {
            Object value;
            try {
                value = f.get(settings);
            } catch (IllegalAccessException e) {
                value = "N/A";
            }
            return "\t" + f.getName() + " = " + value;
        }).collect(Collectors.joining("\n"));
    }

    public void save(AmodeusComponent amodeusComponent, File workingDirectory) throws IOException {
        File settingsFile = new File(workingDirectory, defaultFileName);
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(settingsFile));
            stream.writeObject(update(amodeusComponent).settings);
            stream.close();
            System.out.println("exporting viewer settings to " + settingsFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ViewerConfig update(AmodeusComponent amodeusComponent) {
        settings.zoom = amodeusComponent.getZoom();
        ICoordinate ic = amodeusComponent.getPosition();
        settings.coord = new Coord(ic.getLon(), ic.getLat());
        amodeusComponent.viewerLayers.forEach(viewerLayer -> viewerLayer.updateSettings(settings));
        return this;
    }
}
