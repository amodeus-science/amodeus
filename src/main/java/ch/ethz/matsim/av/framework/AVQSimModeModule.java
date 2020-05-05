package ch.ethz.matsim.av.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerEngineQSimModule;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import ch.ethz.idsc.amodeus.matsim.mod.TrackingHelper;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVData;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVDispatchmentListener;
import ch.ethz.matsim.av.framework.registry.DispatcherRegistry;
import ch.ethz.matsim.av.framework.registry.GeneratorRegistry;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.passenger.AVRequestCreator;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.matsim.av.vrpagent.AVActionCreator;
import ch.ethz.matsim.av.vrpagent.AVAgentSource;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class AVQSimModeModule extends AbstractDvrpModeQSimModule {
    protected AVQSimModeModule(String mode) {
        super(mode);
    }

    @Override
    protected void configureQSim() {
        install(new PassengerEngineQSimModule(getMode()));

        bindModal(PassengerRequestCreator.class).toProvider(modalProvider(getter -> {
            OperatorConfig operatorConfig = getter.getModal(OperatorConfig.class);
            Network network = getter.getModal(Network.class);

            return new AVRequestCreator(operatorConfig.getId(), network, getMode());
        })).in(Singleton.class);

        bindModal(DynActionCreator.class).toProvider(modalProvider(getter -> {
            PassengerEngine passengerEngine = getter.getModal(PassengerEngine.class);
            VrpLegFactory legFactory = getter.getModal(VrpLegFactory.class);
            OperatorConfig operatorConfig = getter.getModal(OperatorConfig.class);

            return new AVActionCreator(passengerEngine, legFactory, operatorConfig.getTimingConfig());
        })).in(Singleton.class);

        bindModal(AVAgentSource.class).toProvider(modalProvider(getter -> {
            DynActionCreator actionCreator = getter.getModal(DynActionCreator.class);
            AVOptimizer optimizer = getter.getModal(AVOptimizer.class);
            QSim qsim = getter.get(QSim.class);
            AVData fleet = getter.getModal(AVData.class);

            return new AVAgentSource(actionCreator, fleet, optimizer, qsim);
        })).in(Singleton.class);

        bindModal(AVDispatchmentListener.class).toProvider(modalProvider(getter -> {
            return new AVDispatchmentListener(getter.getModal(AVDispatcher.class));
        })).in(Singleton.class);

        addModalQSimComponentBinding().to(modalKey(AVDispatchmentListener.class));
        addModalQSimComponentBinding().to(modalKey(AVAgentSource.class));

        // TODO: I think these two can be combined by refactoring AVData, Fleet and those classes
        bindModal(AVData.class).toProvider(new DataProvider(getMode())).in(Singleton.class);
        bindModal(new TypeLiteral<List<AVVehicle>>() {
        }).toProvider(new VehiclesProvider(getMode())).in(Singleton.class);

        bindModal(AVDispatcher.class).toProvider(modalProvider(getter -> {
            OperatorConfig operatorConfig = getter.getModal(OperatorConfig.class);
            String dispatcherName = operatorConfig.getDispatcherConfig().getType();

            AVDispatcher dispatcher = getter.get(DispatcherRegistry.class).get(dispatcherName).createDispatcher(getter);

            for (DvrpVehicle vehicle : getter.getModal(AVData.class).getVehicles().values()) {
                dispatcher.addVehicle((AVVehicle) vehicle);
            }

            return dispatcher;
        })).in(Singleton.class);

        bindModal(AVGenerator.class).toProvider(modalProvider(getter -> {
            OperatorConfig operatorConfig = getter.getModal(OperatorConfig.class);
            String generatorName = operatorConfig.getGeneratorConfig().getType();

            return getter.get(GeneratorRegistry.class).get(generatorName).createGenerator(getter);
        })).in(Singleton.class);

        // TODO: Probably can be removed once we provide AVData directly!
        // And should this go to QSim or Controler scope?
        bindModal(Fleet.class).to(modalKey(AVData.class)).in(Singleton.class);

        bindModal(AVOptimizer.class).toProvider(modalProvider(getter -> {
            EventsManager eventsManager = getter.get(EventsManager.class);
            AVDispatcher dispatcher = getter.getModal(AVDispatcher.class);

            return new AVOptimizer(dispatcher, eventsManager);
        })).in(Singleton.class);
        addModalQSimComponentBinding().to(modalKey(AVOptimizer.class));

        // TODO: Ugly, can we skip this?
        bindModal(VrpOptimizer.class).to(modalKey(AVOptimizer.class)).in(Singleton.class);

        // TODO: Can we replace this?
        bindModal(VrpLegFactory.class).toProvider(modalProvider(getter -> {
            AVOptimizer optimizer = getter.getModal(AVOptimizer.class);
            QSim qsim = getter.get(QSim.class);

            return TrackingHelper.createLegCreatorWithIDSCTracking(optimizer, qsim.getSimTimer());

            /* return new VrpLegFactory() {
             * 
             * @Override
             * public VrpLeg create(DvrpVehicle vehicle) {
             * //return VrpLegFactory.createWithOnlineTracker(TransportMode.car, vehicle, optimizer, qsim.getSimTimer());
             * return TrackingHelper.createLegCreatorWithIDSCTracking(optimizer, qsim.getSimTimer());
             * }
             * }; */
        })).in(Singleton.class);
    }

    static private class DataProvider extends ModalProviders.AbstractProvider<AVData> {
        DataProvider(String mode) {
            super(mode);
        }

        @Override
        public AVData get() {
            Map<Id<DvrpVehicle>, AVVehicle> returnVehicles = new HashMap<>();
            List<AVVehicle> vehicles = getModalInstance(new TypeLiteral<List<AVVehicle>>() {
            });

            for (AVVehicle vehicle : vehicles) {
                vehicle.getSchedule().addTask(new AmodeusStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
                returnVehicles.put(vehicle.getId(), vehicle);
            }

            return new AVData(returnVehicles);
        }
    }

    static private class VehiclesProvider extends ModalProviders.AbstractProvider<List<AVVehicle>> {
        VehiclesProvider(String mode) {
            super(mode);
        }

        @Override
        public List<AVVehicle> get() {
            AVGenerator generator = getModalInstance(AVGenerator.class);

            List<AVVehicle> vehicles = generator.generateVehicles();

            for (AVVehicle vehicle : vehicles) {
                if (Double.isFinite(vehicle.getServiceEndTime())) {
                    throw new IllegalStateException("AV vehicles must have infinite service time");
                }
            }

            return vehicles;
        }
    }
}
