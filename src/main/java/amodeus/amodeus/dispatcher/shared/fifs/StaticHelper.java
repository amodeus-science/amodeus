/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/* package */ enum StaticHelper {
    ;

    public static boolean plansPickupsOrDropoffs(RoboTaxi roboTaxi) {
        if (roboTaxi.getScheduleManager().getDirectives().size() > 0) {
            for (Directive directive : roboTaxi.getScheduleManager().getDirectives()) {
                if (directive instanceof StopDirective) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
