package ch.ethz.idsc.amodeus.scenario.population;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.geo.LinkUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.utils.collections.QuadTree;

import java.io.File;
import java.text.DateFormat;

public class TripPopulationCreator extends AbstractPopulationCreator {

    public TripPopulationCreator(File processingDir, Config config, Network network, MatsimAmodeusDatabase db, //
                                 DateFormat dateFormat) {
        super(processingDir, config, network, db, dateFormat);
    }

    protected void processLine(String[] line, Population population, PopulationFactory populationFactory, //
                               QuadTree<Link> qt) throws Exception {
        // Create Person
        Id<Person> personID = Id.create(reader.get(line, "Id"), Person.class);

        Person person = populationFactory.createPerson(personID);
        Plan plan = populationFactory.createPlan();

        // TODO Choose alternative
        // Coord to link
        int linkIndexStart = LinkUtils.getLinkfromCoord(str2coord(reader.get(line, "PickupLoc")), db, qt);
        int linkIndexEnd = LinkUtils.getLinkfromCoord(str2coord(reader.get(line, "DropoffLoc")), db, qt);
        Id<Link> idStart = db.getOsmLink(linkIndexStart).link.getId();
        Id<Link> idEnd = db.getOsmLink(linkIndexEnd).link.getId();
        // Alternative 2
        // TODO Add conversion to new reference frame
        // Link linkStart = NetworkUtils.getNearestLink(network, mexican.PickupLoc);
        // Link linkEnd = NetworkUtils.getNearestLink(network, mexican.DropoffLoc);
        // Id<Link> linkIdStart = linkStart.getId();
        // Id<Link> linkIdEnd = linkEnd.getId();

        // Start Activity because with have waiting time
        Activity startActivity = populationFactory.createActivityFromLinkId("activity", idStart);

        // Start time = PickupTime - WaitingTime
        double waitTime;
        try {
            waitTime = Double.valueOf(line[6]);
        } catch (Exception e) {
            waitTime = 0.;
        }
        startActivity.setEndTime(dateToSeconds(dateFormat.parse(reader.get(line, "PickupDate"))) - waitTime);

        // Legs
        Leg leg = populationFactory.createLeg("av");
        GlobalAssert.that(startActivity.getEndTime() >= 0);
        leg.setDepartureTime(startActivity.getEndTime() + waitTime);
        GlobalAssert.that(leg.getDepartureTime() >= 0);

        // End Activity
        Activity endActivity = populationFactory.createActivityFromLinkId("activity", idEnd);
        endActivity.setStartTime(dateToSeconds(dateFormat.parse(reader.get(line, "DropoffDate"))));

        // Put together
        plan.addActivity(startActivity);
        plan.addLeg(leg);
        plan.addActivity(endActivity);
        person.addPlan(plan);
        population.addPerson(person);
    }

}
