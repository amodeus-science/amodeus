/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlModifier implements AutoCloseable {

    private final File xmlFile;
    private final Document document;

    public XmlModifier(File xmlFile) throws JDOMException, IOException {
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
