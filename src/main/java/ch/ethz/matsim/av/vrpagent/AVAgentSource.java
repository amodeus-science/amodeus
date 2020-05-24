package ch.ethz.matsim.av.vrpagent;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.schedule.AVOptimizer;

/** Copy & paste from VrpAgentSource, but we want vehicle types per operator **/
public class AVAgentSource implements AgentSource {
    private final DynActionCreator nextActionCreator;
    private final Fleet fleet;
    private final AVOptimizer optimizer;
    private final QSim qSim;

    public AVAgentSource(DynActionCreator nextActionCreator, Fleet fleet, AVOptimizer optimizer, QSim qSim) {
        this.nextActionCreator = nextActionCreator;
        this.fleet = fleet;
        this.optimizer = optimizer;
        this.qSim = qSim;
    }

    @Override
    public void insertAgentsIntoMobsim() {
        VehiclesFactory vehicleFactory = this.qSim.getScenario().getVehicles().getFactory();

        for (DvrpVehicle vrpVeh : fleet.getVehicles().values()) {
            Id<DvrpVehicle> id = vrpVeh.getId();
            Id<Link> startLinkId = vrpVeh.getStartLink().getId();

            AVVehicle avVehicle = (AVVehicle) vrpVeh;
            VehicleType vehicleType = avVehicle.getVehicleType();

            VrpAgentLogic vrpAgentLogic = new VrpAgentLogic(optimizer, nextActionCreator, vrpVeh);
            DynAgent vrpAgent = new DynAgent(Id.createPersonId(id), startLinkId, qSim.getEventsManager(), vrpAgentLogic);
            QVehicle mobsimVehicle = new QVehicleImpl(vehicleFactory.createVehicle(Id.create(id, org.matsim.vehicles.Vehicle.class), vehicleType));
            vrpAgent.setVehicle(mobsimVehicle);
            mobsimVehicle.setDriver(vrpAgent);

            qSim.addParkedVehicle(mobsimVehicle, startLinkId);
            qSim.insertAgentIntoMobsim(vrpAgent);
        }
    }
}
