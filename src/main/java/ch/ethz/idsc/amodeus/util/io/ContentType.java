/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

public enum ContentType {
    APPLICATION_ZIP("application/zip"), //
    TEXT_HTML("text/html"), //
    IMAGE_XICON("image/x-icon"), //
    ;
    private final String expression;

    private ContentType(String expression) {
        this.expression = expression;
    }

    public boolean matches(String string) {
        return expression.equalsIgnoreCase(string);
    }
}
