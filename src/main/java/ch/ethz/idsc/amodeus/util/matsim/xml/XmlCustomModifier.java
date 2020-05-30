/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.matsim.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlCustomModifier implements AutoCloseable {

    private final File xmlFile;
    private final Document document;

    public XmlCustomModifier(File xmlFile) throws JDOMException, IOException {
        if (!xmlFile.isFile())
            throw new RuntimeException("not found: " + xmlFile);

        this.xmlFile = xmlFile;

        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        document = builder.build(xmlFile);
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void close() throws Exception {
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        try (FileWriter fileWriter = new FileWriter(xmlFile)) {
            xmlOutput.output(document, fileWriter);
        }
    }

}
