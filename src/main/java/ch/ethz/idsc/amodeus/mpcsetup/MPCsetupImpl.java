package ch.ethz.idsc.amodeus.mpcsetup;

import java.io.Serializable;

public class MPCsetupImpl implements MPCsetup, Serializable {

    private final long mpcSetupID; 
    private int planningHorizon = 0;
    private int timeStep = 0;
    private boolean milpFlag = false;
    private boolean allowAssistanceFlag = false;
    private double rebalancePunisher = 0.7;
    private boolean firstRebalance = false;


    public MPCsetupImpl() {
        mpcSetupID = System.currentTimeMillis();
    }

    @Override
    public int getPlanningHorizon() {
        return planningHorizon;
    }

    @Override
    public int getTimeStep() {
        return timeStep;
    }

    @Override
    public boolean getMILPflag() {
        return milpFlag;
    }
    
    @Override
    public boolean getAssistanceFlag() {
        return allowAssistanceFlag;
    }
    
    @Override
    public double getRebalancePunisher() {
    	return rebalancePunisher;
    }
    
    public boolean getFirstRebalance() {
    	return firstRebalance;
    }

    public void setPlanningHorizon(int planningHorizon) {
        this.planningHorizon = planningHorizon;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public void setMILPflag(boolean milpFlag) {
        this.milpFlag = milpFlag;
    }
    
    public void setAssistanceFlag(boolean assistanceFlag) {
        this.allowAssistanceFlag = assistanceFlag;
    }
    
    public void setRebalancePunisher(double rebalancePunisher) {
    	this.rebalancePunisher = rebalancePunisher;
    }
    
    public void setFirstRebalance(boolean firstRebalance) {
    	this.firstRebalance = firstRebalance;
    }
    
    
    public long getMPCSetupID() {
        return mpcSetupID;
    }

}
