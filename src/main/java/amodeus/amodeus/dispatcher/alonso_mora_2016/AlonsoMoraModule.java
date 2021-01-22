package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.List;
import java.util.Optional;

import org.matsim.core.controler.AbstractModule;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

import amodeus.amodeus.dispatcher.alonso_mora_2016.routing.DefaultTravelFunction;
import amodeus.amodeus.dispatcher.alonso_mora_2016.routing.DefaultTravelFunction.PartialSolution;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class AlonsoMoraModule extends AbstractModule {
    @Override
    public void install() {
        bind(AlonsoMoraParameters.class).toInstance(new AlonsoMoraParameters());
        bindConstraint(binder()).toInstance(new DefaultTravelFunction.Constraint() {
            @Override
            public boolean validate(Optional<AlonsoMoraVehicle> vehicle, List<StopDirective> directives, PartialSolution partial, double updatedTime, int updatedPassengers) {
                return true;
            }
        });
    }

    static public LinkedBindingBuilder<DefaultTravelFunction.Constraint> bindConstraint(Binder binder) {
        return Multibinder.newSetBinder(binder, DefaultTravelFunction.Constraint.class).addBinding();
    }
}
