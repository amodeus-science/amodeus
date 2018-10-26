package ch.ethz.idsc.amodeus.gfx;

import org.matsim.api.core.v01.Coord;

import java.awt.*;
import java.io.Serializable;

public class ViewerSettings implements Serializable {
    // set default values
    public int zoom = 12;
    public Dimension dimensions = new Dimension(900, 900);
    public Coord coord = null;  // gets replaced by db.getCenter() initially
}
