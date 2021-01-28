/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.beam;

import java.util.LinkedList;
import java.util.List;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/* package */ enum Reorder {
    ;

    /** Changes order of the menu such that first all pickups and then all dropoffs occur.
     * The order is kept. The Redirect Courses are put at the end */
    public static List<Directive> firstAllPickupsThenDropoffs(List<Directive> roboTaxiMenu) {
        List<Directive> newList = new LinkedList<>();
        
        for (Directive directive : roboTaxiMenu) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;
                
                if (stopDirective.isPickup()) {
                    newList.add(directive);
                }
            }
        }
        
        for (Directive directive : roboTaxiMenu) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;
                
                if (!stopDirective.isPickup()) {
                    newList.add(directive);
                }
            }
        }
        
        for (Directive directive : roboTaxiMenu) {
            if (directive instanceof DriveDirective) {
                newList.add(directive);
            }
        }
        
        return newList;
    }
}
