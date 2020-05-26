/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.OsmLink;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.util.math.PolygonArea;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.opt.ConvexHull;
import ch.ethz.idsc.tensor.sca.Sign;

/* package */ class VirtualNodeGeometry {
    private final Tensor inverseArea;
    private final Map<VirtualNode<Link>, Tensor> convexHulls = new LinkedHashMap<>(); // ordering matters

    VirtualNodeGeometry(MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        if (Objects.isNull(virtualNetwork)) {
            inverseArea = null;
            return;
        }
        inverseArea = Array.zeros(virtualNetwork.getVirtualNodes().size());
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            Tensor coords = Tensors.empty();
            for (Link link : virtualNode.getLinks()) {
                OsmLink osmLink = db.getOsmLink(link);
                Coord coord = osmLink.getAt(.5);
                coords.append(Tensors.vector(coord.getX(), coord.getY()));
            }
            Tensor hull = ConvexHull.of(coords);
            convexHulls.put(virtualNode, hull);

            Scalar area = PolygonArea.FUNCTION.apply(hull);
            if (Sign.isPositive(area))
                inverseArea.set(area.reciprocal(), virtualNode.getIndex());
            else
                System.out.println("area[" + virtualNode.getIndex() + "] = " + area);
        }
    }

    Tensor inverseArea() {
        return inverseArea.unmodifiable();
    }

    /***************************************************/
    /** @param amodeusComponent
     * @return */
    Map<VirtualNode<Link>, Shape> getShapes(AmodeusComponent amodeusComponent) {
        Map<VirtualNode<Link>, Shape> map = new LinkedHashMap<>(); // ordering matters
        for (Entry<VirtualNode<Link>, Tensor> entry : convexHulls.entrySet())
            map.put(entry.getKey(), createShape(amodeusComponent, entry.getValue()));
        return map;
    }

    /** @param amodeusComponent
     * @param hull
     * @return */
    static Shape createShape(AmodeusComponent amodeusComponent, Tensor hull) {
        if (Tensors.isEmpty(hull))
            return new Ellipse2D.Double(0, 0, 0, 0);
        Path2D path2d = new Path2D.Double();
        boolean init = false;
        for (Tensor vector : hull) {
            Coord coord = TensorCoords.toCoord(vector);
            Point point = amodeusComponent.getMapPositionAlways(coord);
            if (!init) {
                init = true;
                path2d.moveTo(point.getX(), point.getY());
            } else
                path2d.lineTo(point.getX(), point.getY());
        }
        path2d.closePath();
        return path2d;
    }

    static Shape createShapePixel(AmodeusComponent amodeusComponent, Tensor hull) {
        if (Tensors.isEmpty(hull))
            return new Ellipse2D.Double(0, 0, 0, 0);
        Path2D path2d = new Path2D.Double();
        boolean init = false;
        for (Tensor vector : hull)
            if (!init) {
                init = true;
                path2d.moveTo( //
                        vector.Get(0).number().doubleValue(), //
                        vector.Get(1).number().doubleValue());
            } else
                path2d.lineTo( //
                        vector.Get(0).number().doubleValue(), //
                        vector.Get(1).number().doubleValue());
        path2d.closePath();
        return path2d;
    }

    /***************************************************/
    /** @param amodeusComponent
     * @return */
    Map<VirtualNode<Link>, Tensor> getShapesTensor(AmodeusComponent amodeusComponent) {
        Map<VirtualNode<Link>, Tensor> map = new LinkedHashMap<>(); // ordering matters
        for (Entry<VirtualNode<Link>, Tensor> entry : convexHulls.entrySet())
            map.put(entry.getKey(), createShapeTensor(amodeusComponent, entry.getValue()));
        return map;
    }

    /** @param amodeusComponent
     * @param hull
     * @return */
    private static Tensor createShapeTensor(AmodeusComponent amodeusComponent, Tensor hull) {
        Tensor tensor = Tensors.reserve(hull.length());
        for (Tensor vector : hull) {
            Coord coord = TensorCoords.toCoord(vector);
            Point point = amodeusComponent.getMapPositionAlways(coord);
            tensor.append(Tensors.vectorDouble(point.getX(), point.getY()));
        }
        return tensor;
    }
}
