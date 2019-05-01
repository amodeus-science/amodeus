/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class XmlGeneratorChanger {

    /** Changes generator in "av.xml" file in @param simFolder to the value @param newGenerator
     * 
     * @throws Exception */
    public static void of(File simFolder, String newGenerator) throws Exception {
        System.out.println("changing generator to " + newGenerator);
        File xmlFile = new File(simFolder, "av.xml");
        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());
        try (XmlModifier xmlModifier = new XmlModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild("generator");
            Attribute strategy = dispatcher.getAttribute("strategy");
            if (strategy.getValue() != newGenerator)
                strategy.setValue(newGenerator);
        }
    }

}
