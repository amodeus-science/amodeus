package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Collection;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum StaticHelper {
    ;

    public static void rtCollectionPrinter(Collection<RoboTaxi> roboTaxis, String title, //
            boolean errorPrint) {
        if (!errorPrint) {
            System.out.println(title + ":");
            roboTaxis.stream().forEach(rt -> System.out.print(rt.getId().toString().split(":")[2] + ", "));
            System.out.println("");
        } else {
            System.err.println(title + ":");
            roboTaxis.stream().forEach(rt -> System.err.print(rt.getId().toString().split(":")[2] + ", "));
            System.err.println("");
        }
    }

}
