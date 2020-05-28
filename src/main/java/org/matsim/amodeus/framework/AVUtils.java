package org.matsim.amodeus.framework;

import org.matsim.amodeus.components.AVDispatcher;
import org.matsim.amodeus.components.AVGenerator;
import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.routing.interaction.AVInteractionFinder;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class AVUtils {
    static public LinkedBindingBuilder<AVDispatcher.AVDispatcherFactory> bindDispatcherFactory(Binder binder, String dispatcherName) {
        MapBinder<String, AVDispatcher.AVDispatcherFactory> map = MapBinder.newMapBinder(binder, String.class, AVDispatcher.AVDispatcherFactory.class);
        return map.addBinding(dispatcherName);
    }

    static public LinkedBindingBuilder<AVGenerator.AVGeneratorFactory> bindGeneratorFactory(Binder binder, String generatorName) {
        MapBinder<String, AVGenerator.AVGeneratorFactory> map = MapBinder.newMapBinder(binder, String.class, AVGenerator.AVGeneratorFactory.class);
        return map.addBinding(generatorName);
    }

    static public LinkedBindingBuilder<AVRouter.Factory> bindRouterFactory(Binder binder, String routerName) {
        MapBinder<String, AVRouter.Factory> map = MapBinder.newMapBinder(binder, String.class, AVRouter.Factory.class);
        return map.addBinding(routerName);
    }

    static public LinkedBindingBuilder<AVInteractionFinder.AVInteractionFinderFactory> bindInteractionFinderFactory(Binder binder, String interactionFinderName) {
        MapBinder<String, AVInteractionFinder.AVInteractionFinderFactory> map = MapBinder.newMapBinder(binder, String.class, AVInteractionFinder.AVInteractionFinderFactory.class);
        return map.addBinding(interactionFinderName);
    }

    static public void registerDispatcherFactory(Binder binder, String dispatcherName, Class<? extends AVDispatcher.AVDispatcherFactory> factoryClass) {
        bindDispatcherFactory(binder, dispatcherName).to(factoryClass);
    }

    static public void registerGeneratorFactory(Binder binder, String dispatcherName, Class<? extends AVGenerator.AVGeneratorFactory> factoryClass) {
        bindGeneratorFactory(binder, dispatcherName).to(factoryClass);
    }

    static public void registerRouterFactory(Binder binder, String routerName, Class<? extends AVRouter.Factory> factoryClass) {
        bindRouterFactory(binder, routerName).to(factoryClass);
    }

    static public void registerInteractionFinderFactory(Binder binder, String interactionFinderName, Class<? extends AVInteractionFinder.AVInteractionFinderFactory> factoryClass) {
        bindInteractionFinderFactory(binder, interactionFinderName).to(factoryClass);
    }
}
