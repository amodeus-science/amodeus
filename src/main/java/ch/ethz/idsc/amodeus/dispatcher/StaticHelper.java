/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public enum StaticHelper {
    ;

    /** @return returns the parameter of the av.xml file for the vehicle generator */
    public static String getVehicleGenerator() {
        String vehicleGenerator = null;
        /** reading the number of vehicles out of the av.xml file */
        try {
            File fXmlFile = new File("av.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            NodeList nList = doc.getElementsByTagName("generator");
            vehicleGenerator = ((Element) nList.item(0)).getAttribute("strategy");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehicleGenerator;
    }
}
