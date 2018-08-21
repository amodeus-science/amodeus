package ch.ethz.idsc.amodeus.testutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class StaticHelper {

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

}
