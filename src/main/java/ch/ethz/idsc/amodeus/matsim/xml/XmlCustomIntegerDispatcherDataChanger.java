/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public enum XmlCustomIntegerDispatcherDataChanger {
    ;

    public static void of(File simFolder, String dataName, int newValue) throws Exception {
        System.out.println("changing " + dataName + " to " + newValue);

        File xmlFile = new File(simFolder, "av.xml");

        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());

        try (XmlModifier xmlModifier = new XmlModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element dispatcher = operator.getChild("generator");
            @SuppressWarnings("unchecked")
            List<Element> children = dispatcher.getChildren();
            
            for (Element element : children) {
                @SuppressWarnings("unchecked")
                List<Attribute> theAttributes = element.getAttributes();

                if (theAttributes.get(0).getValue().equals(dataName))
                    theAttributes.get(1).setValue(Integer.toString(newValue));
            }
        }
    }

}