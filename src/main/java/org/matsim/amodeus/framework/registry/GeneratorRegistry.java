package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AmodeusGenerator;

public class GeneratorRegistry extends NamedComponentRegistry<AmodeusGenerator.AVGeneratorFactory> {
    public GeneratorRegistry(Map<String, AmodeusGenerator.AVGeneratorFactory> components) {
        super("AmodeusGenerator", components);
    }
}
