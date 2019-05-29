/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public enum XmlCustomOperatorValueChanger {
    ;

    public static void of(File simFolder, String groupName, String newValue)//
            throws Exception {
        File xmlFile = new File(simFolder, "av.xml");
        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild(groupName);
            Attribute strategy = dispatcher.getAttribute("strategy");
            if (strategy.getValue() != newValue)
                strategy.setValue(newValue);
        }
    }
}
