/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.TensorMap;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.matsim.av.passenger.AVRequest;

public class MatsimAmodeusDatabase {

    public static MatsimAmodeusDatabase initialize(Network network, ReferenceFrame referenceFrame) {
        CoordinateTransformation coords_toWGS84 = referenceFrame.coords_toWGS84();
        List<OsmLink> osmLinks = network.getLinks().values().stream().sorted(Comparator.comparing(link -> link.getId().toString())).map(link -> //
                new OsmLink(link, //
                        coords_toWGS84.transform(link.getFromNode().getCoord()), //
                        coords_toWGS84.transform(link.getToNode().getCoord()) //
                )).collect(Collectors.toList());
        return new MatsimAmodeusDatabase(referenceFrame, osmLinks);
    }

    /** rapid lookup from MATSIM side */
    private final Map<Link, Integer> linkIntegerMap; // TODO remove once no longer needed
    // private final Map<Id<Link>, Integer> linkIdIntegerMap = new HashMap<>();
    private final Map<Integer, OsmLink> integerOsmLinkMap;
    public final ReferenceFrame referenceFrame;

    /** rapid lookup from Viewer */
    private final List<OsmLink> osmLinks;

    // private final IdIntegerDatabase requestIdIntegerDatabase = new IdIntegerDatabase();
    // private final IdIntegerDatabase vehicleIdIntegerDatabase = new IdIntegerDatabase();

    private Integer iteration;

    private MatsimAmodeusDatabase( //
            ReferenceFrame referenceFrame, //
            List<OsmLink> osmLinks) {
        this.referenceFrame = referenceFrame;
        this.osmLinks = osmLinks;
        /*
        int index = 0;
        for (OsmLink osmLink : this.osmLinks) {
            linkIntegerMap.put(osmLink.link, index);
            linkIdIntegerMap.put(osmLink.link.getId(), index);
            ++index;
        }
        */
        linkIntegerMap = this.osmLinks.stream().map(osmLink -> osmLink.link).collect(Collectors.toMap(Function.identity(), link -> link.getId().index()));
        integerOsmLinkMap = this.osmLinks.stream().collect(Collectors.toMap(osmLink -> osmLink.link.getId().index(), Function.identity()));
    }

    public int getLinkIndex(Link link) {
        // Previously this worked by link instance. Now av package uses a subnetwork internally for each dispatcher.
        // This leads to the Link objects being different, but their ID is the same in the general network and in
        // the subnetworks.
        // return linkIdIntegerMap.get(link.getId()); // TODO remove
        return link.getId().index();
    }

    /** @return unmodifiable map that assigns a link to
     *         the corresponding index of the OsmLink in osmLinks
     * @deprecated use {@link Link#getId()} and {@link Id#index()} instead */
    @Deprecated
    public Map<Link, Integer> getLinkIntegerMap() {
        return Collections.unmodifiableMap(linkIntegerMap);
    }

    public OsmLink getOsmLink(int index) {
        // return osmLinks.get(index); // TODO remove
        return integerOsmLinkMap.get(index);
    }

    public Collection<OsmLink> getOsmLinks() {
        return Collections.unmodifiableCollection(osmLinks);
    }

    public Coord getCenter() {
        Tensor points = Tensor.of(getOsmLinks().stream() //
                .map(osmLink -> osmLink.getAt(.5)) //
                .map(TensorCoords::toTensor));
        // Tensor mean = Mean.of(points); // <- mean is fast but doesn't produce as good estimate as median
        Tensor median = TensorMap.of(Median::of, Transpose.of(points), 1);
        return TensorCoords.toCoord(median);
    }

    public int getOsmLinksSize() {
        return osmLinks.size();
    }

    /** @deprecated use {@link AVRequest#getId()} and {@link Id#index()} instead */
    @Deprecated
    public int getRequestIndex(AVRequest avRequest) {
        // return requestIdIntegerDatabase.getId(avRequest.getId().toString()); // TODO remove
        return avRequest.getId().index();
    }

    /** @deprecated use {@link AVRequest#getId()} and {@link Id#index()} instead */
    @Deprecated
    public int getVehicleIndex(RoboTaxi roboTaxi) {
        // return vehicleIdIntegerDatabase.getId(roboTaxi.getId().toString()); // TODO remove
        return roboTaxi.getId().index();
    }

    void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }
}
