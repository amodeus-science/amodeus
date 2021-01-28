package org.matsim.amodeus.drt;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.AmodeusOptimizer;
import org.matsim.amodeus.dvrp.activity.AmodeusActionCreator;
import org.matsim.amodeus.framework.registry.DispatcherRegistry;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.Singleton;

public class AmodeusDrtQSimModeModule extends AbstractDvrpModeQSimModule {
    public AmodeusDrtQSimModeModule(String mode) {
        super(mode);
    }

    @Override
    protected void configureQSim() {
        bindModal(DynActionCreator.class).toProvider(modalProvider(getter -> {
            PassengerEngine passengerEngine = getter.getModal(PassengerEngine.class);
            VrpLegFactory legFactory = getter.getModal(VrpLegFactory.class);
            AmodeusModeConfig modeConfig = getter.getModal(AmodeusModeConfig.class);

            return new AmodeusActionCreator(passengerEngine, legFactory, modeConfig.getTimingConfig());
        })).in(Singleton.class);

        bindModal(AmodeusDispatcher.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String dispatcherName = operatorConfig.getDispatcherConfig().getType();

            AmodeusDispatcher dispatcher = getter.get(DispatcherRegistry.class).get(dispatcherName).createDispatcher(getter);

            for (DvrpVehicle vehicle : getter.getModal(Fleet.class).getVehicles().values()) {
                dispatcher.addVehicle(vehicle);
            }

            return dispatcher;
        })).in(Singleton.class);

        bindModal(AmodeusOptimizer.class).toProvider(modalProvider(getter -> {
            EventsManager eventsManager = getter.get(EventsManager.class);
            AmodeusDispatcher dispatcher = getter.getModal(AmodeusDispatcher.class);

            return new AmodeusOptimizer(dispatcher, eventsManager);
        })).in(Singleton.class);
        addModalQSimComponentBinding().to(modalKey(AmodeusOptimizer.class));

        bindModal(AmodeusDrtOptimizer.class).toProvider(modalProvider(getter -> {
            AmodeusOptimizer delegate = getter.getModal(AmodeusOptimizer.class);
            return new AmodeusDrtOptimizer(delegate);
        })).in(Singleton.class);
        bindModal(VrpOptimizer.class).to(DvrpModes.key(AmodeusDrtOptimizer.class, getMode())).in(Singleton.class);

        bindModal(VrpLegFactory.class).toProvider(modalProvider(getter -> {
            AmodeusOptimizer optimizer = getter.getModal(AmodeusOptimizer.class);
            QSim qsim = getter.get(QSim.class);
            DvrpConfigGroup dvrpConfig = getter.get(DvrpConfigGroup.class);

            return vehicle -> VrpLegFactory.createWithOnlineTracker(dvrpConfig.getMobsimMode(), vehicle, optimizer, qsim.getSimTimer());
        })).in(Singleton.class);
    }
}
