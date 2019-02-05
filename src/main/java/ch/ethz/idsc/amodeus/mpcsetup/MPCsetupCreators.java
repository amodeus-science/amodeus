package ch.ethz.idsc.amodeus.mpcsetup;


import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum MPCsetupCreators {
    ;
        
        public static MPCsetup create(ScenarioOptions scenarioOptions) {
            GlobalAssert.that(scenarioOptions != null);
            MPCsetup mpcSetup = new MPCsetupImpl();
            mpcSetup.setPlanningHorizon(scenarioOptions.getMPCplanningHorizon());
            mpcSetup.setTimeStep(scenarioOptions.getMPCtimeStep());
            mpcSetup.setMILPflag(scenarioOptions.isMPCmilp());
            mpcSetup.setAssistanceFlag(scenarioOptions.allowAssistance());
            mpcSetup.setRebalancePunisher(scenarioOptions.getMPCRebalancePunisher());
            mpcSetup.setFirstRebalance(scenarioOptions.getFirstRebalance());
            mpcSetup.setMaxNumberDrivingEmptyVehicles(scenarioOptions.getMaxDrivingEmptyVehicles());
            return mpcSetup;
        }
}
