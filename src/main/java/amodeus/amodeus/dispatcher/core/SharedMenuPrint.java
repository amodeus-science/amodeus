/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.dispatcher.shared.SharedMenu;

/* package */ enum SharedMenuPrint {
    ;

    public static void of(SharedMenu sharedMenu) {
        of(sharedMenu.getCourseList());
    }

    public static void of(List<SharedCourse> sharedMenu) {
        sharedMenu.forEach(sc -> {
            System.out.println("\tavRequest: " + sc.getAvRequest().getId().toString()//
                    + "\tlink: :" + sc.getLink().getId().toString()//
                    + "\tcourse id: " + sc.getCourseId()//
                    + "\tmeal type: " + sc.getMealType().name());//
        });
    }
}
