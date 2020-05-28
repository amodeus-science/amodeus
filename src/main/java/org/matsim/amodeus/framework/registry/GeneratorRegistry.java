package org.matsim.amodeus.framework.registry;

import java.util.Map;

import org.matsim.amodeus.components.AVGenerator;

public class GeneratorRegistry extends NamedComponentRegistry<AVGenerator.AVGeneratorFactory> {
    public GeneratorRegistry(Map<String, AVGenerator.AVGeneratorFactory> components) {
        super("AVGenerator", components);
    }
}
