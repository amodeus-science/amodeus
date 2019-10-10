package ch.ethz.idsc.amodeus.linkspeed.create;

import java.io.File;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class SolveInitialDelete {

    public static void main(String[] args) {

        Tensor flowMatrix = Tensors.fromString("{{1,1,0,0},{0,0,1,1},{0,0,1,1}}");
        Tensor deviationMatrix = Tensors.fromString("{{0.3},{0.4},{0.4}}");
        int numVar = 4;
        int numFlowConstraints = 3;

        /** setting up the LP */
        glp_prob lp;
        glp_smcp parm; // Unsure what this does
        SWIGTYPE_p_int ind;
        SWIGTYPE_p_double val;
        int retour;

        lp = GLPK.glp_create_prob(); // Create Problem and assign to problem variable lp
        System.out.println("Problem Created");
        GLPK.glp_set_prob_name(lp, "ClaudioLP");

        /** variables */
        GLPK.glp_add_cols(lp, numVar);
        for (int i = 1; i <= numVar; ++i) {
            System.err.println("i:" + i);
            String name = "x" + i;
            GLPK.glp_set_col_name(lp, i, name);
            GLPK.glp_set_col_kind(lp, i, GLPKConstants.GLP_CV); // stands for continuous variable variable
            GLPK.glp_set_col_bnds(lp, i, GLPKConstants.GLP_LO, 0.0, 10);
        }

        /** flow constraints */
        GLPK.glp_add_rows(lp, numFlowConstraints); // We have 6 constraints i.e. we need 6 rows
        for (int i = 1; i <= numFlowConstraints; ++i) {
            Tensor flow = flowMatrix.get(i - 1);
            GLPK.glp_set_row_name(lp, i, ("Constr" + i)); // We are setting an Auxiliary Variable
            double rhs = deviationMatrix.Get(i - 1, 0).number().doubleValue();
            GLPK.glp_set_row_bnds(lp, i, GLPKConstants.GLP_FX, rhs, 1); // We are setting a Double Bound (lower: 0, upper: 1)
            ind = GLPK.new_intArray(12); // TODO Change no of elements, Indexes
            val = GLPK.new_doubleArray(12); // TODO change no of elements, Values
            for (int j = 1; j <= numVar; ++j) {
                int value = flow.Get(j - 1).number().intValue();
                GLPK.intArray_setitem(ind, j, j); // First element is x11 * 1
                GLPK.doubleArray_setitem(val, j, value);
            }
            GLPK.glp_set_mat_row(lp, i, numVar, ind, val);
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);
        }

        /** objective function, simple minimization of sum_i(xi) */
        GLPK.glp_set_obj_name(lp, "MinimalDeviations");
        GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
        for (int i = 1; i <= numVar; ++i) {
            GLPK.glp_set_obj_coef(lp, i, -1);
            GLPK.glp_set_obj_coef(lp, i, 0.8);
        }

        /** Solving and writing to file */
        parm = new glp_smcp();
        GLPK.glp_init_smcp(parm);
        retour = GLPK.glp_simplex(lp, parm);

        // Retrieve solution
        if (retour == 0) {
            write_lp_solution(lp);
        } else {
            System.out.println("The problem could not be solved");
        }

        File file = new File("/home/clruch/Desktop/debugLP.lp");
        GLPK.glp_write_lp(lp, null, file.getAbsolutePath());

    }

    static void write_lp_solution(glp_prob lp) {
        int i;
        int n;
        String name;
        double val;

        name = GLPK.glp_get_obj_name(lp);
        val = GLPK.glp_get_obj_val(lp);
        System.out.print(name);
        System.out.print(" = ");
        System.out.println(val);
        n = GLPK.glp_get_num_cols(lp);
        for (i = 1; i <= n; i++) {
            name = GLPK.glp_get_col_name(lp, i);
            val = GLPK.glp_get_col_prim(lp, i);
            System.out.print(name);
            System.out.print(" = ");
            System.out.println(val);
        }

    }

}
