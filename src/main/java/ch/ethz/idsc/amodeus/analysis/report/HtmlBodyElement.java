/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

public class HtmlBodyElement {

    private final HtmlGenerator htmlGenerator = new HtmlGenerator();

    /* package */ void append(HtmlBodyElement htmlBodyElement) {
        htmlGenerator.newLine();
        htmlGenerator.appendHTMLGenerator(htmlBodyElement.getHTMLGenerator());
    }

    public HtmlGenerator getHTMLGenerator() {
        return htmlGenerator;
    }
}
