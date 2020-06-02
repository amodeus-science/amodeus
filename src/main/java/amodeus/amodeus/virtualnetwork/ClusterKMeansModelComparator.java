/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork;

import java.util.Comparator;

import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;

/* package */ enum ClusterKMeansModelComparator implements Comparator<Cluster<KMeansModel>> {
    INSTANCE;

    @Override
    public int compare(Cluster<KMeansModel> o1, Cluster<KMeansModel> o2) {
        return Double.compare( //
                o1.getModel().getPrototype().get(0), //
                o2.getModel().getPrototype().get(0) //
        );
    }

}
