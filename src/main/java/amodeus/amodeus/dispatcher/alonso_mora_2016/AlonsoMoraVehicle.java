package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;

public interface AlonsoMoraVehicle {
    public Id<DvrpVehicle> getId();

    public Link getLocation();

    public List<Directive> getDirectives();

    public int getCapacity();

    public RoboTaxi getVehicle();
}
