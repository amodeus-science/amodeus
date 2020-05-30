/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.TensorMap;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.red.Median;

public class MatsimAmodeusDatabase implements IterationStartsListener, IterationEndsListener {

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
    private final Map<Integer, OsmLink> integerOsmLinkMap;
    public final ReferenceFrame referenceFrame;

    /** rapid lookup from Viewer */
    private final List<OsmLink> osmLinks;

    private Integer iteration;

    private MatsimAmodeusDatabase( //
            ReferenceFrame referenceFrame, //
            List<OsmLink> osmLinks) {
        this.referenceFrame = referenceFrame;
        this.osmLinks = osmLinks;
        integerOsmLinkMap = this.osmLinks.stream().collect(Collectors.toMap(osmLink -> osmLink.link.getId().index(), Function.identity()));
    }

    public OsmLink getOsmLink(Link link) {
        return getOsmLink(link.getId().index());
    }

    public OsmLink getOsmLink(int index) {
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

    void setIteration(Integer iteration) {
        this.iteration = iteration;
    }

    public int getIteration() {
        return iteration;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        setIteration(null);
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        setIteration(event.getIteration());
    }
}
