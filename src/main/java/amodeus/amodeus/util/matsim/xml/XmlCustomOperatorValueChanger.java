/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.matsim.xml;

import java.io.File;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

public enum XmlCustomOperatorValueChanger {
    ;

    public static void of(File simFolder, String groupName, String newValue) throws Exception {
        File xmlFile = new File(simFolder, "av.xml");
        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild(groupName);
            Attribute strategy = dispatcher.getAttribute("strategy");
            if (!strategy.getValue().equals(newValue))
                strategy.setValue(newValue);
        }
    }
}
