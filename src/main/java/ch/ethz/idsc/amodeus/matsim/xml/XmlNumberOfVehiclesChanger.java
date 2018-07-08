/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public enum XmlNumberOfVehiclesChanger {
    ;

    public static void of(File simFolder, int vehicleNumber) throws Exception {
        System.out.println("changing vehicle number to " + vehicleNumber);

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

                if (theAttributes.get(0).getValue().equals("numberOfVehicles"))
                    theAttributes.get(1).setValue(Integer.toString(vehicleNumber));
            }
        }
    }

}
