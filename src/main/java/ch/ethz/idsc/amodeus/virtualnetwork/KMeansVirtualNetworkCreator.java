/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.virtualnetwork.core.AbstractVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNodes;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;

public class KMeansVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {
    private static final SquaredEuclideanDistanceFunction DISTANCE_FUNCTION = SquaredEuclideanDistanceFunction.STATIC;

    private Clustering<KMeansModel> clustering;

    /** @param datapoints of any kind with coordinates that should be used for k-Means generation of the network
     * @param elements elements that should be grouped in {@link VirtualNode} clusters
     * @param uElements set of elements U and a list of all T they are associated to
     * @param locationOf
     * @param nameOf
     * @param lbounds
     * @param ubounds
     * @param numVNodes
     * @param completeGraph */
    public KMeansVirtualNetworkCreator( //
            double data[][], Collection<T> elements, Map<U, HashSet<T>> uElements, Function<T, Tensor> locationOf, //
            Function<T, String> nameOf, Tensor lbounds, Tensor ubounds, int numVNodes, boolean completeGraph, //
            int tryIterations) {
        virtualNetwork = createVirtualNetwork(data, elements, uElements, locationOf, nameOf, lbounds, ubounds, numVNodes, completeGraph, tryIterations);
    }

    private VirtualNetwork<T> createVirtualNetwork( //
            double data[][], Collection<T> elements, Map<U, HashSet<T>> uElements, Function<T, Tensor> locationOf, //
            Function<T, String> nameOf, Tensor lbounds, Tensor ubounds, int numVNodes, boolean completeGraph, //
            int tryIterations) {

        long initSeed = 1;
        int iterations = 0;
        while (iterations < tryIterations) {
            System.out.println("trying to create K-means virtual network, attempt: " + iterations);

            Map<VirtualNode<T>, Set<T>> vNodeTMap = createAssignmentMap(data, elements, uElements, locationOf, nameOf, //
                    lbounds, ubounds, numVNodes, completeGraph, initSeed);

            /** create */
            virtualNetwork = createVirtualNetwork(vNodeTMap, elements, uElements, nameOf, completeGraph);

            if (virtualNetwork.getVirtualNodes().size() == numVNodes) {
                return virtualNetwork;
            }
            ++initSeed;
            ++iterations;
        }
        System.err.println("Not possible to create a virtual network with desired number of nodes usnig k-means, try reducing number of nodes.");
        return null;

    }

    private Map<VirtualNode<T>, Set<T>> createAssignmentMap( //
            double data[][], Collection<T> elements, Map<U, HashSet<T>> uElements, Function<T, Tensor> locationOf, //
            Function<T, String> nameOf, Tensor lbounds, Tensor ubounds, int numVNodes, boolean completeGraph, long initSeed) {

        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.get(initSeed));

        // initialize new virtual network
        // VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

        // 1) COMPUTE CLUSTERING with k-means method based on the supplied data -> every node with same amount of datapoints
        // adapter to load data from an existing array.
        DatabaseConnection databaseConnection = new ArrayAdapterDatabaseConnection(data);
        // Create a database (which may contain multiple relations!)
        Database database = new StaticArrayDatabase(databaseConnection, null);
        // Load the data into the database (do NOT forget to initialize...)
        database.initialize();

        // Setup textbook k-means clustering:
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(DISTANCE_FUNCTION, numVNodes, 1000, init);

        // Run the algorithm:
        clustering = km.run(database);

        // CREATE MAP with all VirtualNodes
        // the datastructure HAS TO BE a linked hash map ! do not change to hash map
        // the map has to be ordered to preserve the indexing of the vnodes 0,1,2,...
        Map<VirtualNode<T>, Set<T>> vNMap = new LinkedHashMap<>();

        {
            List<Cluster<KMeansModel>> allClusters = clustering.getAllClusters();
            Collections.sort(allClusters, ClusterKMeansModelComparator.INSTANCE);
            int index = 0;
            for (Cluster<KMeansModel> cluster : allClusters) {
                Tensor coord = Tensors.vectorDouble( //
                        cluster.getModel().getPrototype().get(0), //
                        cluster.getModel().getPrototype().get(1));
                String indexStr = VirtualNodes.getIdString(index);
                vNMap.put(new VirtualNode<T>(index, indexStr, new HashMap<>(), coord), new LinkedHashSet<T>());
                ++index;
            }
        }

        // 2) ASSIGN network links to closest nodes with a quadtree structure
        VNodeAdd.byProximity(vNMap, lbounds, ubounds, elements, locationOf);
        return vNMap;
    }

    public Clustering<KMeansModel> getClustering() {
        return clustering;
    }
}
