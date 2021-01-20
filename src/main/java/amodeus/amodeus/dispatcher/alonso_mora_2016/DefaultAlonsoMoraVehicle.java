package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;

public class DefaultAlonsoMoraVehicle implements AlonsoMoraVehicle {
    private final RoboTaxi delegate;

    public DefaultAlonsoMoraVehicle(RoboTaxi delegate) {
        this.delegate = delegate;
    }

    public Id<DvrpVehicle> getId() {
        return delegate.getId();
    }

    public Link getLocation() {
        return delegate.getDivertableLocation();
    }

    public List<Directive> getDirectives() {
        return delegate.getScheduleManager().getDirectives();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DefaultAlonsoMoraVehicle) {
            DefaultAlonsoMoraVehicle otherVehicle = (DefaultAlonsoMoraVehicle) other;
            return otherVehicle.getId().equals(getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public int getCapacity() {
        return delegate.getCapacity();
    }

    @Override
    public RoboTaxi getVehicle() {
        return delegate;
    }
}
