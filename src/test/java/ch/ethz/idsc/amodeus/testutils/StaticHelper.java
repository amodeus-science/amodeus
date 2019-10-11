/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum StaticHelper {
    ;

    public static void changeDispatcherTo(String newDispatcher, File simFolder) //
            throws JDOMException, IOException {
        System.out.println("changing fare ratio to " + newDispatcher);

        File xmlFile = new File(simFolder, "av.xml");

        System.out.println("looking for av.xml file at " + xmlFile.getAbsolutePath());

        GlobalAssert.that(xmlFile.exists());

        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document doc = builder.build(xmlFile);
        Element rootNode = doc.getRootElement();
        Element operator = rootNode.getChild("operator");
        Element dispatcher = operator.getChild("dispatcher");

        Attribute strategy = dispatcher.getAttribute("strategy");
        if (strategy.getValue() != newDispatcher)
            strategy.setValue(newDispatcher);

        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, new FileWriter(xmlFile));

    }

    public static void setup() {
        for (LocationSpec locationSpec : TestLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);
    }

}
