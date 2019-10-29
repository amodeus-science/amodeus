/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class ILPConstruction {

    protected glp_prob lp;
    protected SWIGTYPE_p_int ind; // used to get c arrays
    protected static SWIGTYPE_p_double val;

    /** closing the LP in order to release allocated memory */
    public final void closeLP() {
        // release storage allocated for LP
        GLPK.glp_delete_prob(lp);
    }

    public void defineLP(List<TripWithVehicle> grossListOfRTVEdges, List<AVRequest> openRequestList, //
            List<RoboTaxi> listOfRoboTaxiWithValidTrip, double costOfIgnoredReuqestNormal, double costOfIgnoredReuqestHigh, //
            Set<AVRequest> requestMatchedLastStep) {
        // define some variables
        int numOfRTVEdges = grossListOfRTVEdges.size();
        int numOfTaxiInILP = listOfRoboTaxiWithValidTrip.size();
        int numOfRequest = openRequestList.size();
        int numOfVariables = numOfRTVEdges + numOfRequest;
        int numOfConstraints = numOfTaxiInILP + numOfRequest;
        int numOfVariablesPlus1 = numOfVariables + 1;

        try {
            // Create problem
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created");

            GLPK.glp_set_prob_name(lp, "TripAssignment");
            // Define columns
            GLPK.glp_add_cols(lp, numOfVariables);
            for (int i = 1; i < numOfVariables + 1; i++) {
                GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_IV); // stands for integer variable
                GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_DB, 0, 1);// stands for double bound, 0 is lower bound,
                                                                         // 1 is upper bound
            }

            /** constrain */
            // Create constraints (define rows)
            GLPK.glp_add_rows(lp, numOfConstraints);

            // row 1 to row numOfTaxiInILP, no more than 1 trip per vehicle (inequality constraints (<=1))
            // Note: the constraints of no more than 1 trip assigned to 1 request is included by the equality constraints below
            for (int i = 1; i <= numOfTaxiInILP; i++) {
                GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_UP, 0, 1);
                ind = GLPK.new_intArray(numOfVariablesPlus1); // use number of optimization variables + 1
                for (int j = 1; j < numOfVariablesPlus1; j++)
                    GLPK.intArray_setitem(ind, j, j);
                val = GLPK.new_doubleArray(numOfVariablesPlus1); // use number of optimization variables + 1
                // writing coefficient of each variable in constraint
                // active part
                for (int j = 1; j <= numOfRTVEdges; j++)
                    GLPK.doubleArray_setitem(val, j, grossListOfRTVEdges.get(j - 1).getRoboTaxi() == listOfRoboTaxiWithValidTrip.get(i - 1) ? 1. : 0.);
                // inactive Part (all entries are 0)
                for (int j = numOfRTVEdges + 1; j < numOfVariablesPlus1; j++)
                    GLPK.doubleArray_setitem(val, j, 0.);
                // store in constraint row i
                GLPK.glp_set_mat_row(lp, i, numOfVariables, ind, val); // read function description (lp, row number, numOfVarialbes, ind, val)
            }

            // row numOfTaxiInILP+1 to row numOfConstraints, a request is either ignored or accepted (equality constraints (=1))
            for (int i = numOfTaxiInILP + 1; i < numOfConstraints + 1; i++) {
                GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_FX, 1, 1);
                ind = GLPK.new_intArray(numOfVariablesPlus1); // use number of optimization variables + 1
                for (int j = 1; j < numOfVariablesPlus1; j++)
                    GLPK.intArray_setitem(ind, j, j);
                val = GLPK.new_doubleArray(numOfVariablesPlus1); // use number of optimization variables + 1
                // writing coefficient of each variables in this constraint
                // request is accepted part
                for (int j = 1; j <= numOfRTVEdges; j++)
                    // if request is contained in the trip, then the coefficient of that trip is 1
                    GLPK.doubleArray_setitem(val, j, grossListOfRTVEdges.get(j - 1).getTrip().contains(openRequestList.get(i - numOfTaxiInILP - 1)) ? 1. : 0.);
                // request is ignored part (an identity matrix)
                for (int j = numOfRTVEdges + 1; j < numOfVariablesPlus1; j++)
                    GLPK.doubleArray_setitem(val, j, j - numOfRTVEdges == i - numOfTaxiInILP ? 1. : 0.);
                // store in constraint row i
                GLPK.glp_set_mat_row(lp, i, numOfVariables, ind, val); // read function description (lp, row number, numOfVarialbes, ind, val)
            }

            /** objective */
            // Define objective
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN); // to minimize

            // request is accepted part
            for (int i = 1; i <= numOfRTVEdges; i++)
                GLPK.glp_set_obj_coef(lp, i, grossListOfRTVEdges.get(i - 1).getTotalDelay());

            // request is ignored part
            // TODO done. Checking if it will work
            for (int i = numOfRTVEdges + 1; i < numOfVariablesPlus1; i++)
                GLPK.glp_set_obj_coef(lp, i, requestMatchedLastStep.contains(openRequestList.get(i - numOfRTVEdges - 1)) ? costOfIgnoredReuqestHigh : costOfIgnoredReuqestNormal);
        } catch (GlpkException ex) {
            ex.printStackTrace();
        }
    }

    public void solveLP(boolean mute) {
        // System.out.println("solving LP: " + GLPK.glp_get_prob_name(lp));

        glp_iocp parm = new glp_iocp(); // different in MIP
        GLPK.glp_init_iocp(parm); // different in MIP
        parm.setPresolve(GLPK.GLP_ON); // GLPK documentation at p. 59
        int ret = GLPK.glp_intopt(lp, parm); // different in MIP -> other method for solving
        int stat = GLPK.glp_mip_status(lp); // different in MIP

        try {
            if (ret != 0) // ret==0 indicates of the algorithm ran correctly
                throw new RuntimeException("something went wrong");
            if (stat == GLPK.GLP_NOFEAS)
                throw new RuntimeException(/* "LP has found infeasible solution" */);
            if (stat != GLPK.GLP_OPT)
                throw new RuntimeException(/* "LP has found suboptimal solution" */);
        } catch (RuntimeException e) {
            closeLP();
            throw e;
        }
    }

    public List<Double> writeLPSolution() {
        System.out.println("\nThe solution is:\n");

        /* double val = */ GLPK.glp_mip_obj_val(lp); // different in MIP
        // System.out.println("Objective value:");
        // System.out.println(val + "\n");

        // System.out.println("Column variables:");
        int n = GLPK.glp_get_num_cols(lp);
        return IntStream.rangeClosed(1, n).mapToObj(i -> GLPK.glp_mip_col_val(lp, i)).collect(Collectors.toList()); // different in MIP
    }
}
