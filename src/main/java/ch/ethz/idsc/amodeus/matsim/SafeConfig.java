/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import java.util.Objects;
import java.util.function.Function;

import org.matsim.core.config.ReflectiveConfigGroup;

public class SafeConfig {
    public static SafeConfig wrap(ReflectiveConfigGroup reflectiveConfigGroup) {
        return new SafeConfig(reflectiveConfigGroup);
    }

    // ---
    private final ReflectiveConfigGroup reflectiveConfigGroup;

    protected SafeConfig(ReflectiveConfigGroup reflectiveConfigGroup) {
        this.reflectiveConfigGroup = Objects.requireNonNull(reflectiveConfigGroup, "reflective group == null");
    }

    public int getInteger(String key, int alt) {
        return get(key, alt, Integer::parseInt);
    }

    public double getDouble(String key, double alt) {
        return get(key, alt, Double::parseDouble);
    }

    public String getString(String key, String alt) {
        return get(key, alt, s -> s);
    }

    private <T> T get(String key, T alt, Function<String, T> parser) {
        try {
            return getStrict(key, parser);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return alt;
    }

    public int getIntegerStrict(String key) {
        return getStrict(key, Integer::parseInt);
    }

    public double getDoubleStrict(String key) {
        return getStrict(key, Double::parseDouble);
    }

    public boolean getBoolStrict(String key) {
        return getStrict(key, Boolean::parseBoolean);
    }

    public String getStringStrict(String key) {
        return getStrict(key, s -> s);
    }

    private <T> T getStrict(String key, Function<String, T> parser) {
        String string = Objects.requireNonNull(reflectiveConfigGroup.getParams().get(key));
        return parser.apply(string);
    }
}
