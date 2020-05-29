package org.matsim.amodeus.framework;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.routing.interaction.AmodeusInteractionFinder;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class AVUtils {
    static public LinkedBindingBuilder<AmodeusDispatcher.AVDispatcherFactory> bindDispatcherFactory(Binder binder, String dispatcherName) {
        MapBinder<String, AmodeusDispatcher.AVDispatcherFactory> map = MapBinder.newMapBinder(binder, String.class, AmodeusDispatcher.AVDispatcherFactory.class);
        return map.addBinding(dispatcherName);
    }

    static public LinkedBindingBuilder<AmodeusGenerator.AVGeneratorFactory> bindGeneratorFactory(Binder binder, String generatorName) {
        MapBinder<String, AmodeusGenerator.AVGeneratorFactory> map = MapBinder.newMapBinder(binder, String.class, AmodeusGenerator.AVGeneratorFactory.class);
        return map.addBinding(generatorName);
    }

    static public LinkedBindingBuilder<AmodeusRouter.Factory> bindRouterFactory(Binder binder, String routerName) {
        MapBinder<String, AmodeusRouter.Factory> map = MapBinder.newMapBinder(binder, String.class, AmodeusRouter.Factory.class);
        return map.addBinding(routerName);
    }

    static public LinkedBindingBuilder<AmodeusInteractionFinder.AVInteractionFinderFactory> bindInteractionFinderFactory(Binder binder, String interactionFinderName) {
        MapBinder<String, AmodeusInteractionFinder.AVInteractionFinderFactory> map = MapBinder.newMapBinder(binder, String.class, AmodeusInteractionFinder.AVInteractionFinderFactory.class);
        return map.addBinding(interactionFinderName);
    }

    static public void registerDispatcherFactory(Binder binder, String dispatcherName, Class<? extends AmodeusDispatcher.AVDispatcherFactory> factoryClass) {
        bindDispatcherFactory(binder, dispatcherName).to(factoryClass);
    }

    static public void registerGeneratorFactory(Binder binder, String dispatcherName, Class<? extends AmodeusGenerator.AVGeneratorFactory> factoryClass) {
        bindGeneratorFactory(binder, dispatcherName).to(factoryClass);
    }

    static public void registerRouterFactory(Binder binder, String routerName, Class<? extends AmodeusRouter.Factory> factoryClass) {
        bindRouterFactory(binder, routerName).to(factoryClass);
    }

    static public void registerInteractionFinderFactory(Binder binder, String interactionFinderName, Class<? extends AmodeusInteractionFinder.AVInteractionFinderFactory> factoryClass) {
        bindInteractionFinderFactory(binder, interactionFinderName).to(factoryClass);
    }
}
