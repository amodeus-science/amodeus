package ch.ethz.idsc.amodeus.mpcsetup;


public interface MPCsetup {

    /** @return {@link int} the planning horizon for the MPC controller */
    int getPlanningHorizon();

    /** @return {@link int} the time step in minutes for the MPC controller */
    int getTimeStep();
    
    /** @return {@link boolean} solve mixed integer linear program or linear program */
    boolean getMILPflag();
    
    /** @return {@link boolean} allow assistance for robustness */
    boolean getAssistanceFlag();
    
    /** @return {@link double} the rebalance punisher for the MPC controller */
    double getRebalancePunisher();
    
    /** @return {@link int} the planning horizon for the MPC controller */
    void setPlanningHorizon(int planningHorizon);

    /** @return {@link int} the time step in minutes for the MPC controller */
    void setTimeStep(int timeStep);
    
    /** @return {@link boolean} solve mixed integer linear program or linear program */
    void setMILPflag(boolean milpFlag);
    
    /** @return {@link boolean} allow assistance for robustness */
    void setAssistanceFlag(boolean assistanceFlag);
    
    /** @return {@link double} the rebalance punisher for the MPC controller */
    void setRebalancePunisher(double rebalancePunisher);

}
