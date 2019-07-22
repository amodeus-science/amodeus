/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

public enum XmlCustomDataChanger {
    ;

    /** Changes the value @param dataName in @param groupName in the av.xml
     * file in {@link File} @param simFolder to @param newValue, usage example:
     * 
     * XmlCustomIntegerDispatcherDataChanger.of(simFolder, "numberOfVehicles", "generator",vehicleNumber);
     * 
     * @throws Exception */

    public static void of(File simFolder, String groupName, String dataName, String newValue)//
            throws Exception {
        System.out.println("changing " + dataName + " to " + newValue);
        File xmlFile = new File(simFolder, "av.xml");
        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());
        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element operator = rootNode.getChild("operator");
            Element group = operator.getChild(groupName);
            @SuppressWarnings("unchecked")
            List<Element> children = group.getChildren();
            for (Element element : children) {
                @SuppressWarnings("unchecked")
                List<Attribute> theAttributes = element.getAttributes();
                if (theAttributes.get(0).getValue().equals(dataName)) {
                    theAttributes.get(1).setValue(newValue);
                }
            }
        }
    }

}