package amodeus.amodeus.dispatcher.alonso_mora_2016;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public interface AlonsoMoraTravelFunction {
    /** This function is called by the Alonso-Mora dispatcher to find the optimal
     * order of pickups and dropoffs for a given vehicle and a list of requests.
     * The method should consider a range of constraints and may not return any result
     * if the constraints cannot be fulfilled. */
    public Optional<Result> calculate(AlonsoMoraVehicle vehicle, Collection<AlonsoMoraRequest> requests);

    /** This function is called by the Alonso-Mora dispatcher when creating the RV graph. It
     * should return the cost of combining two requests, if they can be combined (considering
     * the constraints and assuming that a vehicle is available either at the first request's
     * location or at the second request's location). */
    public Optional<Result> calculate(AlonsoMoraRequest firstRequest, AlonsoMoraRequest secondRequest);

    static public class Result {
        public final List<StopDirective> directives;
        public final double cost;

        public Result(List<StopDirective> directives, double cost) {
            this.cost = cost;
            this.directives = directives;
        }
    }
}
