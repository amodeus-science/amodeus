/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.matsim.api.core.v01.network.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.Magnitude;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;

public enum LPUtils {
    ;
    /* package */ static final Scalar DURATION = Quantity.of(24 * 60 * 60, SI.SECOND);
    /* package */ static final Scalar AVERAGE_VEL = Quantity.of(30, "km*h^-1");

    /** Takes the Euclidean distance between the centers of the virtual stations
     * and derives the travel time for a given constant velocity.
     *
     * 
     * @param velocity in [km/h]
     * @return tensor with travel time between the virtual stations in [s], e.g. output.get(i,j) is the travel
     *         time from virtual station i to j */
    /* package */ static Tensor getEuclideanTravelTimeBetweenVSCenters(VirtualNetwork<Link> virtualNetwork, Scalar velocity) {
        double velocityMperS = Magnitude.VELOCITY.toDouble(velocity); // in m/s
        int nVNodes = virtualNetwork.getvNodesCount();
        Tensor travelTime = Array.zeros(nVNodes, nVNodes);
        for (VirtualLink<Link> link : virtualNetwork.getVirtualLinks()) {
            int sourceIndex = link.getFrom().getIndex();
            int sinkIndex = link.getTo().getIndex();

            travelTime.set(RealScalar.of(link.getDistance() / velocityMperS), sourceIndex, sinkIndex);

        }
        return travelTime;
    }

    /** @return returns the parameter of the av.xml file for the number of vehicles */
    public static int getNumberOfVehicles() {
        int numberVehicles = 0;
        /** reading the number of vehicles out of the av.xml file */
        try {
            File fXmlFile = new File("av.xml"); // TODO still hard coded, try to read it from {@link Config}
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            NodeList nList = doc.getElementsByTagName("generator");
            Element elem = (Element) nList.item(0);
            Element subelem = (Element) elem.getElementsByTagName("param").item(0);
            numberVehicles = Integer.parseInt(subelem.getAttribute("value"));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("av.xml or av_v1.dtd file not found!!! Using default value for number of vehicles instead, which is 100!");
            numberVehicles = 100;
        }
        return numberVehicles;
    }

    /** @param tensor
     * @return the rounded vector where non-negativity and almost integer elements are required, else an exception is thrown */
    /* package */ static Tensor getRoundedRequireNonNegative(Tensor tensor) {
        Tensor rounded = Round.of(tensor);
        GlobalAssert.that(Chop._04.close(tensor, rounded));
        rounded.flatten(-1).forEach(element -> Sign.requirePositiveOrZero(element.Get()));
        return rounded;
    }

    /** @param tensor
     * @return the rounded vector where almost integer elements are required, else an exception is thrown */
    /* package */ static Tensor getRounded(Tensor tensor) {
        Tensor rounded = Round.of(tensor);
        GlobalAssert.that(Chop._04.close(tensor, rounded));
        return rounded;
    }
}
