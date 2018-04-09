/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ch.ethz.idsc.amodeus.view.jmapviewer.interfaces.ICoordinate;

class AmodeusComponentMouse extends MouseAdapter {
    private ICoordinate iCoordinate;
    private Point point;
    private final AmodeusComponent amodeusComponent;

    public AmodeusComponentMouse(AmodeusComponent amodeusComponent) {
        this.amodeusComponent = amodeusComponent;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        iCoordinate = amodeusComponent.getPosition(mouseEvent.getPoint());
        point = amodeusComponent.getMapPosition(iCoordinate.getLat(), iCoordinate.getLon());
        amodeusComponent.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        iCoordinate = amodeusComponent.getPosition(mouseEvent.getPoint());
        point = amodeusComponent.getMapPosition(iCoordinate.getLat(), iCoordinate.getLon());
    }

    public ICoordinate getICoordinate() {
        return iCoordinate;
    }

    public Point getPoint() {
        return point;
    }

}
