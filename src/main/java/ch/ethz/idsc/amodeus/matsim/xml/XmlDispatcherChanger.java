/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public enum XmlDispatcherChanger {
    ;

    public static void of(File simFolder, String newDispatcher) throws Exception {
        System.out.println("changing dispatcher to " + newDispatcher);

        File xmlFile = new File(simFolder, "av.xml");

        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());

        try (XmlModifier xmlModifier = new XmlModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild("dispatcher");

            Attribute strategy = dispatcher.getAttribute("strategy");
            if (strategy.getValue() != newDispatcher)
                strategy.setValue(newDispatcher);
        }

    }

}
