/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed.create;

import java.io.File;
import java.io.IOException;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import ch.ethz.idsc.amodeus.linkspeed.create.TrafficDelayEstimate;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.io.Export;

public enum GLPKLinOptDelayCalculator implements TrafficDelayEstimate {
    INSTANCE;

    @Override
    public Tensor compute(Tensor flowMatrix, Tensor deviation) {

        /** problem characteristics */
        int numRoads = Dimensions.of(flowMatrix).get(1);
        int numTrips = Dimensions.of(flowMatrix).get(0);
        double pRoad = 1.0;
        double pSlack = 10000.0;
        System.out.println("Dimensions: ");
        System.out.println("roads: " + numRoads);
        System.out.println("trips: " + numTrips);
        GlobalAssert.that(Dimensions.of(flowMatrix).get(0) == deviation.length());
        long tStart = System.currentTimeMillis();

        /** setting up the LP */
        glp_prob lp;
        glp_smcp parm; // Unsure what this does
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int retour;

        lp = GLPK.glp_create_prob(); // Create Problem and assign to problem variable lp
        System.out.println("Problem Created");
        GLPK.glp_set_prob_name(lp, "Traffic Flow LP");

        /** variables for roads */
        GLPK.glp_add_cols(lp, numRoads);
        for (int i = 1; i <= numRoads; ++i) {
            String name = "x" + i;
            GLPK.glp_set_col_name(lp, i, name);
            GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV); // stands for continuous variable variable
            GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0.0, 10);
        }

        /** one slack variable per constraint */
        GLPK.glp_add_cols(lp, numTrips);
        for (int i = numRoads + 1; i <= numRoads + numTrips; ++i) {
            String name = "s" + (i - numRoads);
            GLPK.glp_set_col_name(lp, i, name);
            GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV); // stands for continuous variable variable
            GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_FR, 0.0, 10);
        }

        /** one additional variable to have the absolute value of the slack variable in the
         * objective function */
        GLPK.glp_add_cols(lp, numTrips);
        for (int i = numRoads + numTrips + 1; i <= numRoads + numTrips + numTrips; ++i) {
            String name = "z" + (i - numRoads - numTrips);
            GLPK.glp_set_col_name(lp, i, name);
            GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV); // stands for continuous variable variable
            GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0.0, 10);
        }

        /** flow constraints */
        GLPK.glp_add_rows(lp, numTrips);
        // int arraySize = numRoads + 2 * numTrips + 2;
        int arraySize = numRoads + numTrips;
        for (int i = 1; i <= numTrips; ++i) {
            // name of constraint
            GLPK.glp_set_row_name(lp, i, ("TripConstr" + i)); // We are setting an Auxiliary Variable
            // right hand side
            double rhs = deviation.Get(i - 1, 0).number().doubleValue();
            GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_FX, rhs, 1); // We are setting a Double Bound (lower: 0, upper: 1)
            // initialize arrays
            ind = GLPK.new_intArray(arraySize); // TODO Change no of elements, Indexes
            val = GLPK.new_doubleArray(arraySize); // TODO change no of elements, Values
            // flow defines multiplier for variable
            Tensor flow = flowMatrix.get(i - 1);
            for (int j = 1; j <= numRoads; ++j) {
                int value = flow.Get(j - 1).number().intValue();
                GLPK.intArray_setitem(ind, j, j); // First element is x11 * 1
                GLPK.doubleArray_setitem(val, j, value);
            }
            // adding 1 slack variable to each constraint to ensure feasibility
            GLPK.intArray_setitem(ind, (1 + numRoads), (i + numRoads)); // First element is x11 * 1
            GLPK.doubleArray_setitem(val, (1 + numRoads), 1.0);
            // add constraints
            GLPK.glp_set_mat_row(lp, i, (1 + numRoads), ind, val);
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);
        }

        /** constraints to ensure minimzation of absolute value, si - zi <=0 */
        GLPK.glp_add_rows(lp, 2 * numTrips);
        for (int i = 1; i <= numTrips; ++i) {
            int constraintindex = i + numTrips;
            int constraintindex2 = i + numTrips * 2;

            // set constraint si-zi <= 0
            GLPK.glp_set_row_name(lp, constraintindex, ("SlackConstrUp" + (constraintindex - numTrips)));
            GLPK.glp_set_row_bnds(lp, constraintindex, GLPKConstants.GLP_UP, -2.3215, 0.0); // We are setting a Double Bound (lower: 0, upper: 1)
            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);
            GLPK.intArray_setitem(ind, 1, i + numRoads);
            GLPK.doubleArray_setitem(val, 1, 1.0);
            GLPK.intArray_setitem(ind, 2, i + numRoads + numTrips);
            GLPK.doubleArray_setitem(val, 2, -1.0);
            GLPK.glp_set_mat_row(lp, constraintindex, 2, ind, val);
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);

            // set constraint -si-zi <= 0
            GLPK.glp_set_row_name(lp, constraintindex2, ("SlackConstrUp" + (constraintindex2 - numTrips)));
            GLPK.glp_set_row_bnds(lp, constraintindex2, GLPKConstants.GLP_UP, -2.3215, 0.0); // We are setting a Double Bound (lower: 0, upper: 1)
            ind = GLPK.new_intArray(3);
            val = GLPK.new_doubleArray(3);
            GLPK.intArray_setitem(ind, 1, i + numRoads);
            GLPK.doubleArray_setitem(val, 1, -1.0);
            GLPK.intArray_setitem(ind, 2, i + numRoads + numTrips);
            GLPK.doubleArray_setitem(val, 2, -1.0);
            GLPK.glp_set_mat_row(lp, constraintindex2, 2, ind, val);
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);
        }

        /** objective function, simple minimization of sum_i(xi) */
        GLPK.glp_set_obj_name(lp, "MinimalDeviations");
        GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
        // // cost for road variables
        // for (int i = 1; i <= numRoads; ++i) {
        // GLPK.glp_set_obj_coef(lp, i, -1);
        // GLPK.glp_set_obj_coef(lp, i, pRoad);
        // }

        // cost for slack variables
        for (int i = numRoads + numTrips + 1; i <= numRoads + 2 * numTrips; ++i) {
            GLPK.glp_set_obj_coef(lp, i, -1);
            GLPK.glp_set_obj_coef(lp, i, pSlack);
        }

        /** Solving and writing to file */
        parm = new glp_smcp();
        GLPK.glp_init_smcp(parm);
        retour = GLPK.glp_simplex(lp, parm);

        // Retrieve solution
        if (retour == 0) {
            // solution found
        } else {
            System.out.println("The problem could not be solved");
        }

        File file = new File("/home/clruch/Desktop/debugLP.lp");
        GLPK.glp_write_lp(lp, null, file.getAbsolutePath());

        Tensor trafficDelay = Tensors.matrix((i, j) -> (RealScalar.of(GLPK.glp_get_col_prim(lp, i + 1))), numRoads, 1);
        Tensor slack = Tensors.matrix((i, j) -> (RealScalar.of(GLPK.glp_get_col_prim(lp, i + 1 + numRoads))), numTrips, 1);
        lp.delete();

        try {
            Export.of(new File("/home/clruch/Desktop/trafficDelayGLPK.csv"), trafficDelay);
            Export.of(new File("/home/clruch/Desktop/trafficDelayGLPKslack.csv"), slack);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Duration: " + (System.currentTimeMillis() - tStart) + " [ms]");

        return trafficDelay;
    }

}
