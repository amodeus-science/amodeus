/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;

public enum XmlVehicleGeneratorChanger {
    ;

    public static void of(File simFolder, String generatorName) throws Exception {
        System.out.println("changing vehicle generator to " + generatorName);

        File xmlFile = new File(simFolder, "av.xml");

        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());

        try (XmlModifier xmlModifier = new XmlModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element generator = operator.getChild("generator");
            generator.setAttribute("strategy", generatorName);

        }

    }

}
