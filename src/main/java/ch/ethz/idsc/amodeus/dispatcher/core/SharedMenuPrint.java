/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;

public class SharedMenuPrint {
    ;

    public static void of(SharedMenu sharedMenu) {
        of(sharedMenu.getRoboTaxiMenu());
    }

    public static void of(List<SharedCourse> sharedMenu) {
        sharedMenu.stream().forEach(sc -> {
            System.out.println("\tavRequest: " + sc.getAvRequest().getId().toString()//
                    + "\tlink: :" + sc.getLink().getId().toString()//
                    + "\tcourse id: " + sc.getCourseId()//
                    + "\tmeal type: " + sc.getMealType().name());//
        });
    }
}
