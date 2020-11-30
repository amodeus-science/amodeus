package org.matsim.amodeus.framework;

import java.util.LinkedList;
import java.util.List;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.dvrp.AmodeusOptimizer;
import org.matsim.amodeus.dvrp.activity.AmodeusActionCreator;
import org.matsim.amodeus.dvrp.request.AmodeusRequestCreator;
import org.matsim.amodeus.framework.registry.DispatcherRegistry;
import org.matsim.amodeus.framework.registry.GeneratorRegistry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerEngineQSimModule;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSourceQSimModule;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;

public class AmodeusModeQSimModule extends AbstractDvrpModeQSimModule {
    public AmodeusModeQSimModule(AmodeusModeConfig modeConfig) {
        super(modeConfig.getMode());
    }

    @Override
    protected void configureQSim() {
        install(new PassengerEngineQSimModule(getMode()));

        bindModal(PassengerRequestCreator.class).toProvider(modalProvider(getter -> {
            return new AmodeusRequestCreator(getMode());
        })).in(Singleton.class);

        bindModal(DynActionCreator.class).toProvider(modalProvider(getter -> {
            PassengerEngine passengerEngine = getter.getModal(PassengerEngine.class);
            VrpLegFactory legFactory = getter.getModal(VrpLegFactory.class);
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);

            return new AmodeusActionCreator(passengerEngine, legFactory, operatorConfig.getTimingConfig());
        })).in(Singleton.class);

        install(new VrpAgentSourceQSimModule(getMode()));

        bindModal(AmodeusDispatcher.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String dispatcherName = operatorConfig.getDispatcherConfig().getType();

            AmodeusDispatcher dispatcher = getter.get(DispatcherRegistry.class).get(dispatcherName).createDispatcher(getter);

            for (DvrpVehicle vehicle : getter.getModal(Fleet.class).getVehicles().values()) {
                dispatcher.addVehicle(vehicle);
            }

            return dispatcher;
        })).in(Singleton.class);

        bindModal(AmodeusGenerator.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String generatorName = operatorConfig.getGeneratorConfig().getType();

            return getter.get(GeneratorRegistry.class).get(generatorName).createGenerator(getter);
        })).in(Singleton.class);

        bindModal(Fleet.class).toProvider(new FleetProvider(getMode())).in(Singleton.class);

        bindModal(AmodeusOptimizer.class).toProvider(modalProvider(getter -> {
            EventsManager eventsManager = getter.get(EventsManager.class);
            AmodeusDispatcher dispatcher = getter.getModal(AmodeusDispatcher.class);

            return new AmodeusOptimizer(dispatcher, eventsManager);
        })).in(Singleton.class);
        addModalQSimComponentBinding().to(modalKey(AmodeusOptimizer.class));
        bindModal(VrpOptimizer.class).to(DvrpModes.key(AmodeusOptimizer.class, getMode())).in(Singleton.class);

        bindModal(VrpLegFactory.class).toProvider(modalProvider(getter -> {
            AmodeusOptimizer optimizer = getter.getModal(AmodeusOptimizer.class);
            QSim qsim = getter.get(QSim.class);
            DvrpConfigGroup dvrpConfig = getter.get(DvrpConfigGroup.class);

            return vehicle -> VrpLegFactory.createWithOnlineTracker(dvrpConfig.getMobsimMode(), vehicle, optimizer, qsim.getSimTimer());
        })).in(Singleton.class);
    }

    static private class FleetProvider extends ModalProviders.AbstractProvider<Fleet> {
        FleetProvider(String mode) {
            super(mode);
        }

        @Override
        public Fleet get() {
            AmodeusGenerator generator = getModalInstance(AmodeusGenerator.class);
            Network network = getModalInstance(Network.class);

            List<DvrpVehicleSpecification> specifications = generator.generateVehicles();
            List<DvrpVehicle> vehicles = new LinkedList<>();

            for (DvrpVehicleSpecification specification : specifications) {
                if (Double.isFinite(specification.getServiceEndTime())) {
                    throw new IllegalStateException("AV vehicles must have infinite service time");
                }

                vehicles.add(new DvrpVehicleImpl(specification, network.getLinks().get(specification.getStartLinkId())));
            }

            for (DvrpVehicle vehicle : vehicles) {
                vehicle.getSchedule().addTask(new DrtStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
            }

            return () -> vehicles.stream().collect(ImmutableMap.toImmutableMap(DvrpVehicle::getId, v -> v));
        }
    }
}
