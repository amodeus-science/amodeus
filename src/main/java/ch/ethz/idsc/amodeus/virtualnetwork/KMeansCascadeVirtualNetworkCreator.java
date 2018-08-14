/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.prep.Request;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
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

public class KMeansCascadeVirtualNetworkCreator {

    private DatabaseConnection dbc;
    private Database db;
    private SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
    // private RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);

    private KMeansLloyd<NumberVector> km;
    private Clustering<KMeansModel> c;
    private VirtualNetwork<Link> virtualNetwork;
    private Map<VirtualNode<Link>, Set<Link>> vNMapGlobal = new LinkedHashMap<>();
    private Function<Link, Tensor> locationOf;

    /** @param requests
     * @param elements
     * @param uElements
     * @param locationOf
     * @param nameOf
     * @param numVNodes required to be a number of two potency
     * @param completeGraph
     * @param tryIterations
     * 
     *            The area is split into two parts by k-Means and then the sub-parts are again split until there are numVNodes many areas */
    public KMeansCascadeVirtualNetworkCreator(Set<Request> requests, Collection<Link> elements, Map<Node, HashSet<Link>> uElements, Function<Link, Tensor> locationOf, //
            Function<Link, String> nameOf, int numVNodes, boolean completeGraph, //
            int tryIterations) {
        // make sure numVNodes is power of 2
        for (int i = numVNodes; i > 1;) {
            GlobalAssert.that(i % 2 == 0);
            i /= 2;
        }

        this.locationOf = locationOf;

        createSubVirtualNetwork(requests, elements, numVNodes, tryIterations, null, 1, 0);

        Tensor bounds = getBounds(elements);
        // ASSIGN network links to closest nodes with a quadtree structure
        CreatorUtils.addByProximity(vNMapGlobal, bounds.get(0), bounds.get(1), elements, locationOf);

        // initialize new virtual network
        virtualNetwork = new VirtualNetworkImpl<>();

        CreatorUtils.addToVNodes(vNMapGlobal, nameOf, virtualNetwork);

        // create virtualLinks for complete or neighboring graph
        VirtualLinkBuilder.build(virtualNetwork, completeGraph, uElements);
        GlobalAssert.that(VirtualNetworkCheck.virtualLinkConsistencyCheck(virtualNetwork));

        // fill information for serialization
        CreatorUtils.fillSerializationInfo(elements, virtualNetwork, nameOf);
    }

    private void createSubVirtualNetwork(Set<Request> requests, Collection<Link> elements, int numVNodes, int tryIterations, Tensor coord, int level, int subIndex) {
        if (numVNodes == 1) {
            fillGlobalMap(subIndex, coord);
        } else {
            System.out.println("Creating split on level " + level + " and index " + subIndex);
            Map<VirtualNode<Link>, Set<Link>> vNMapSplit = getKmeanSplit(requests, elements, tryIterations);
            List<Entry<VirtualNode<Link>, Set<Link>>> linksList = new ArrayList<>(vNMapSplit.entrySet());
            Set<Link> links0 = linksList.get(0).getValue();
            Set<Link> links1 = linksList.get(1).getValue();
            Tensor coord0 = linksList.get(0).getKey().getCoord();
            Tensor coord1 = linksList.get(1).getKey().getCoord();
            Set<Request> requests0 = Request.filterLinks(requests, links0);
            Set<Request> requests1 = Request.filterLinks(requests, links1);
            createSubVirtualNetwork(requests0, links0, numVNodes / 2, tryIterations, coord0, level + 1, subIndex);
            createSubVirtualNetwork(requests1, links1, numVNodes / 2, tryIterations, coord1, level + 1, subIndex + numVNodes / 2);
        }
    }

    private Map<VirtualNode<Link>, Set<Link>> getKmeanSplit(Set<Request> requests, Collection<Link> elements, int tryIterations) {
        long initSeed = 1;
        int iterations = 0;
        double data[][] = getData(requests);
        Tensor bounds = getBounds(elements);
        while (iterations < tryIterations) {
            System.out.println("trying to create K-means virtual network, attempt: " + iterations);
            Map<VirtualNode<Link>, Set<Link>> vNMap = solveKmeanSplit(data, elements, bounds, initSeed);
            if (vNMap.size() == 2) {
                return vNMap;
            }
            ++initSeed;
            ++iterations;
        }
        System.err.println("Not possible to create a virtual network with desired number of nodes usnig k-means, try reducing number of nodes.");
        return null;
    }

    private Map<VirtualNode<Link>, Set<Link>> solveKmeanSplit(double[][] data, Collection<Link> elements, Tensor bounds, long initSeed) {

        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.get(initSeed));

        // 1) COMPUTE CLUSTERING with k-means method based on the supplied data -> every node with same amount of datapoints
        // adapter to load data from an existing array.
        dbc = new ArrayAdapterDatabaseConnection(data);
        // Create a database (which may contain multiple relations!)
        db = new StaticArrayDatabase(dbc, null);
        // Load the data into the database (do NOT forget to initialize...)
        db.initialize();

        // Setup textbook k-means clustering:
        km = new KMeansLloyd<>(dist, 2, 1000, init);

        // Run the algorithm:
        c = km.run(db);

        // CREATE MAP with all VirtualNodes
        // the datastructure HAS TO BE a linked hash map ! do not change to hash map
        // the map has to be ordered to preserve the indexing of the vnodes 0,1,2,...
        Map<VirtualNode<Link>, Set<Link>> vNMap = new LinkedHashMap<>();

        {

            Map<Double, Cluster<KMeansModel>> sortedMap = new TreeMap<>();
            c.getAllClusters().stream().forEach(c -> //
            sortedMap.put(c.getModel().getPrototype().get(0), c));

            int index = 0;
            for (Cluster<KMeansModel> clu : sortedMap.values()) {
                Tensor coord = Tensors.vectorDouble(clu.getModel().getPrototype().get(0), clu.getModel().getPrototype().get(1));
                String indexStr = VirtualNodes.getIdString(index);
                vNMap.put(new VirtualNode<Link>(index, indexStr, new HashMap<>(), coord), new LinkedHashSet<Link>());
                index++;
            }
        }

        // 2) ASSIGN network links to closest nodes with a quadtree structure
        CreatorUtils.addByProximity(vNMap, bounds.get(0), bounds.get(1), elements, locationOf);

        return vNMap;
    }

    private void fillGlobalMap(int index, Tensor coord) {
        System.out.println("Creating virtual node " + index);
        String indexStr = VirtualNodes.getIdString(index);
        vNMapGlobal.put(new VirtualNode<Link>(index, indexStr, new HashMap<>(), coord), new LinkedHashSet<Link>());
    }

    private static Tensor getBounds(Collection<Link> elements) {
        Set<Node> nodes = new HashSet<>();
        elements.stream().forEach(link -> nodes.add(link.getFromNode()));
        elements.stream().forEach(link -> nodes.add(link.getToNode()));
        double[] bounds = NetworkUtils.getBoundingBox(nodes);

        return Tensors.of(Tensors.vector(bounds[0], bounds[1]), Tensors.vector(bounds[2], bounds[3]));
    }

    private static double[][] getData(Set<Request> requests) {
        List<Coord> coords = new ArrayList<>();
        requests.stream().forEach(request -> coords.add(request.startLink().getCoord()));

        final double data[][] = new double[coords.size()][2];
        for (int i = 0; i < coords.size(); ++i) {
            data[i][0] = coords.get(i).getX();
            data[i][1] = coords.get(i).getY();
        }
        return data;
    }

    public VirtualNetwork<Link> getVirtualNetwork() {
        return virtualNetwork;
    }

    public Clustering<KMeansModel> getClustering() {
        return c;
    }

}
