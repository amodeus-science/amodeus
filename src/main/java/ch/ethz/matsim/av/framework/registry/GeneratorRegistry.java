package ch.ethz.matsim.av.framework.registry;

import java.util.Map;

import ch.ethz.matsim.av.generator.AVGenerator;

public class GeneratorRegistry extends NamedComponentRegistry<AVGenerator.AVGeneratorFactory> {
    public GeneratorRegistry(Map<String, AVGenerator.AVGeneratorFactory> components) {
        super("AVGenerator", components);
    }
}
