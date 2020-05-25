package ch.ethz.matsim.av.framework;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Network;
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

import ch.ethz.matsim.av.config.AmodeusModeConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.registry.DispatcherRegistry;
import ch.ethz.matsim.av.framework.registry.GeneratorRegistry;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.passenger.AVRequestCreator;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.refactoring.dvrp.activity.AmodeusActionCreator;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class AVQSimModeModule extends AbstractDvrpModeQSimModule {
    protected AVQSimModeModule(AmodeusModeConfig modeConfig) {
        super(modeConfig.getMode());
    }

    @Override
    protected void configureQSim() {
        install(new PassengerEngineQSimModule(getMode()));

        bindModal(PassengerRequestCreator.class).toProvider(modalProvider(getter -> {
            Network network = getter.getModal(Network.class);
            return new AVRequestCreator(getMode(), network);
        })).in(Singleton.class);

        bindModal(DynActionCreator.class).toProvider(modalProvider(getter -> {
            PassengerEngine passengerEngine = getter.getModal(PassengerEngine.class);
            VrpLegFactory legFactory = getter.getModal(VrpLegFactory.class);
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);

            return new AmodeusActionCreator(passengerEngine, legFactory, operatorConfig.getTimingConfig());
        })).in(Singleton.class);

        install(new VrpAgentSourceQSimModule(getMode()));

        bindModal(AVDispatcher.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String dispatcherName = operatorConfig.getDispatcherConfig().getType();

            AVDispatcher dispatcher = getter.get(DispatcherRegistry.class).get(dispatcherName).createDispatcher(getter);

            for (DvrpVehicle vehicle : getter.getModal(Fleet.class).getVehicles().values()) {
                dispatcher.addVehicle(vehicle);
            }

            return dispatcher;
        })).in(Singleton.class);

        bindModal(AVGenerator.class).toProvider(modalProvider(getter -> {
            AmodeusModeConfig operatorConfig = getter.getModal(AmodeusModeConfig.class);
            String generatorName = operatorConfig.getGeneratorConfig().getType();

            return getter.get(GeneratorRegistry.class).get(generatorName).createGenerator(getter);
        })).in(Singleton.class);

        bindModal(Fleet.class).toProvider(new FleetProvider(getMode())).in(Singleton.class);

        bindModal(AVOptimizer.class).toProvider(modalProvider(getter -> {
            EventsManager eventsManager = getter.get(EventsManager.class);
            AVDispatcher dispatcher = getter.getModal(AVDispatcher.class);

            return new AVOptimizer(dispatcher, eventsManager);
        })).in(Singleton.class);
        addModalQSimComponentBinding().to(modalKey(AVOptimizer.class));
        bindModal(VrpOptimizer.class).to(DvrpModes.key(AVOptimizer.class, getMode())).in(Singleton.class);

        bindModal(VrpLegFactory.class).toProvider(modalProvider(getter -> {
            AVOptimizer optimizer = getter.getModal(AVOptimizer.class);
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
            AVGenerator generator = getModalInstance(AVGenerator.class);
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
                vehicle.getSchedule().addTask(new AmodeusStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
            }

            return () -> vehicles.stream().collect(ImmutableMap.toImmutableMap(DvrpVehicle::getId, v -> v));
        }
    }
}
