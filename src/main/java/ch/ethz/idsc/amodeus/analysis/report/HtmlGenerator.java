/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class HtmlGenerator {
    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean html = false;
    private boolean head = false;
    private boolean header = false;
    private boolean body = false;
    private boolean footer = false;
    private boolean style = false;

    public HtmlGenerator() {
    }

    /** called at the begin and end of document */
    /* package */ void html() {
        if (html) {
            stringBuilder.append("</html>");
            html = false;
        } else {
            stringBuilder.append("<html>");
            html = true;
            this.insertCSS("header, footer {font-family: arial;", //
                    "background-color: #000099;", //
                    "color: white;", //
                    "padding: 20px;", //
                    "float: left;", //
                    "width: 100%}", //
                    "body {font-family: verdana;", //
                    "font-size: 16px;", //
                    "line-height: 1.75;}", //
                    "img {padding: 5px;}", //
                    "#footer_link {color: white;}");
        }
    }

    /** called at the begin and end of document */
    public void body() {
        manifest(body, "body");
        body ^= true;
    }

    public void newLine() {
        stringBuilder.append("<br style=\"clear:both\">");
    }

    /** called at the begin and end of footer */
    public void footer() {
        manifest(footer, "footer");
        footer ^= true;
    }

    public void insertImgRight(String relPath, int width, int heigth) {
        stringBuilder.append("<img id=\"img_right\" float=\"right\" src=" + relPath + " " + //
                "alt=\"Image not found\" style=\"width:" + width + "px;height:" + heigth + "px;\">");
    }

    public void insertSubTitle(String title) {
        stringBuilder.append("<h2>" + title + "</h2>");
    }

    public void title(String title) {
        insertTitle(title);
        setTitle(title);
    }

    public void insertLink(String url, String link) {
        this.insertCSS("a {font-family: arial;}");
        if (footer || header)
            stringBuilder.append("<a id=\"footer_link\" target=\"_blank\"" + " href=\"" + url + "\"> <b>" + link + "</b></a>");
        else
            stringBuilder.append("<a target=\"_blank\" href=\"" + url + "\"> <b>" + link + "</b></a>");
    }

    public void insertCSS(String... line) {
        head();
        style();
        Stream.of(line).forEachOrdered(stringBuilder::append);
        style();
        head();
    }

    public void insertImg(String relPath, int width, int heigth) {
        stringBuilder.append("<img src=" + relPath + " alt=\"Image not found\" style=\"width:" + //
                width + "px;height:" + heigth + "px;\">");
    }

    public void insertImg(File file, int width, int heigth) {
        insertImg(file.getName(), width, heigth);
    }

    public static String bold(String text) {
        return "<b>" + text + "</b>";
    }

    /* package */ void saveFile(String fileName, File reportDirectory) throws IOException {
        GlobalAssert.that(reportDirectory.isDirectory());
        File file = new File(reportDirectory, fileName + ".html");
        // if file does exists, then delete and create a new file
        Files.deleteIfExists(file.toPath());

        // write to file with OutputStreamWriter
        try (OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile())) {
            try (Writer writer = new OutputStreamWriter(outputStream)) {
                writer.write(stringBuilder.toString());
            }
        }
        System.out.println("Exported " + file.getName());
    }

    private void manifest(boolean status, String tag) {
        stringBuilder.append("<" + (status ? "/" : "") + tag + ">");
    }

    /** called at the begin and end of head */
    private void head() {
        manifest(head, "head");
        head ^= true;
    }

    /** called at the begin and end of header */
    private void header() {
        manifest(header, "header");
        header ^= true;
    }

    /** called at the begin and end of CSS */
    private void style() {
        if (style) {
            stringBuilder.append("</style>");
            style = false;
        } else {
            stringBuilder.append("<style>");
            style = true;
        }
    }

    /** tab title
     *
     * @param title */
    private void setTitle(String title) {
        head();
        stringBuilder.append("<title>" + title + "</title>");
        head();
    }

    /** page title
     *
     * @param title */
    private void insertTitle(String title) {
        header();
        stringBuilder.append("<h1>" + title + "</h1>");
        header();
    }

    public void insertTextLeft(String text) {
        stringBuilder.append("<pre id=\"pre_left\">" + text + "</pre>");
    }

    /* package */ void appendHTMLGenerator(HtmlGenerator htmlGenerator) {
        stringBuilder.append(htmlGenerator.getStringBuilder());
    }

    private StringBuilder getStringBuilder() {
        return stringBuilder;
    }

}
