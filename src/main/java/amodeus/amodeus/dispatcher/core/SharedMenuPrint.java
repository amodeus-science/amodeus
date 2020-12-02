/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/* package */ enum SharedMenuPrint {
    ;

    public static void of(List<Directive> directives) {
        for (Directive directive : directives) {
            if (directive instanceof StopDirective) {
                StopDirective stop = (StopDirective) directive;
                Link link = stop.isPickup() ? stop.getRequest().getFromLink() : stop.getRequest().getToLink();

                System.out.println("\tavRequest: " + stop.getRequest().getId().toString()//
                        + "\tlink: :" + link.getId().toString()//
                        + "\tcourse id: " + stop.getRequest().getId()//
                        + "\tmeal type: " + (stop.isPickup() ? "PICKUP" : "DROPOFF"));//
            }
        }
    }
}
