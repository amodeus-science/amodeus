/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.ICoordinate;
import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.TileSource;
import ch.ethz.idsc.amodeus.view.jmapviewer.tilesources.MapnikTileSource;

public class ViewerConfig {
    private static final String DEFAULT_FILENAME = "viewerSettings";

    public static ViewerConfig fromDefaults(MatsimAmodeusDatabase db) {
        return new ViewerConfig(db);
    }

    public static ViewerConfig from(AmodeusComponent amodeusComponent) {
        return new ViewerConfig(amodeusComponent.db).update(amodeusComponent);
    }

    public static ViewerConfig from(MatsimAmodeusDatabase db, File workingDirectory) throws IOException {
        File settingsFile = new File(workingDirectory, DEFAULT_FILENAME);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(settingsFile))) {
            ViewerSettings settings = (ViewerSettings) objectInputStream.readObject();
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

    // ---
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
        File settingsFile = new File(workingDirectory, DEFAULT_FILENAME);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsFile))) {
            objectOutputStream.writeObject(update(amodeusComponent).settings);
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

    public TileSource getTileSource() {
        try {
            return MapSource.valueOf(settings.tileSourceName).getTileSource();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return MapnikTileSource.INSTANCE;
        }
    }
}
