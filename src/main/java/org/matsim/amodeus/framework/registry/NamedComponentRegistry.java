package org.matsim.amodeus.framework.registry;

import java.util.HashMap;
import java.util.Map;

/** This class is basically just a helper, because currently the ModalProviders getter does not have a getter for TypeLiterals. */
public class NamedComponentRegistry<T> {
    private final String subject;
    private final Map<String, T> components = new HashMap<>();

    public NamedComponentRegistry(String subject, Map<String, T> components) {
        this.subject = subject;
        this.components.putAll(components);
    }

    public T get(String name) {
        T component = components.get(name);

        if (component == null) {
            throw new IllegalStateException(String.format("%s with name '%s' does not exist!", subject, name));
        }

        return components.get(name);
    }
}
