/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

// TODO document file..
public class GLPKAssignmentSolverBetter {

    private Tensor cost_matrix;
    public Tensor last_sol;
    private glp_prob lp;
    private int requests;
    private int vehicles;
    private int variables;
    // private Tensor changes;
    private Scalar obj_val;
    private Tensor c_ij;
    private Tensor p_ij;
    private final double alpha;// = 1;
    private final double beta;// = 0.15;
    private final double gamma;// = 0.9;

    public GLPKAssignmentSolverBetter(Tensor costFunctionWeights) {
        alpha = costFunctionWeights.Get(0).number().doubleValue();
        beta = costFunctionWeights.Get(1).number().doubleValue();
        gamma = costFunctionWeights.Get(2).number().doubleValue();
        this.lp = GLPK.glp_create_prob(); // Create Problem and assign to problem variable lp
        System.out.println("Problem Created");
        GLPK.glp_set_prob_name(lp, "Tryouts");
    }

    public void defineStructuralVariables(int var_counter) {
        /* Add variables xij */
        GLPK.glp_add_cols(lp, variables);
        for (int i = 1; i <= requests; ++i)
            for (int j = 1; j <= vehicles; ++j) {
                String varName = "x(t)" + i + j;
                GLPK.glp_set_col_name(lp, var_counter, varName);
                GLPK.glp_set_col_kind(lp, var_counter, GLPKConstants.GLP_BV);
                GlobalAssert.that(var_counter <= variables);
                ++var_counter;
            }
    }

    public void defineConstraints() {
        int len = Math.max(vehicles, requests);
        SWIGTYPE_p_int ind = GLPK.new_intArray(len + 1);
        SWIGTYPE_p_double val = GLPK.new_doubleArray(len + 1);
        int num_constraints = requests + vehicles;
        int num_rows_lp = GLPK.glp_get_num_rows(lp); // Already existing number of rows
        if (num_constraints > num_rows_lp)
            GLPK.glp_add_rows(lp, num_constraints - num_rows_lp); // Prevents memory overflow
        else if (num_constraints < num_rows_lp)
            throw new RuntimeException(); // Currently it is not possible to remove rows

        // Constraint: Only one Assignment per Request
        int constraint_counter = 1;
        for (int i = 1; i <= requests; ++i) {
            String name = "Constr" + constraint_counter;
            GLPK.glp_set_row_name(lp, constraint_counter, name); // We are setting an Auxiliary Variable
            GLPK.glp_set_row_bnds(lp, constraint_counter, GLPKConstants.GLP_DB, 0, 1); // We are setting a Double Bound (lower: 0, upper: 1)
            for (int j = 1; j <= vehicles; ++j) {
                GLPK.intArray_setitem(ind, j, j + vehicles * (i - 1));
                GLPK.doubleArray_setitem(val, j, 1);
            }
            GLPK.glp_set_mat_row(lp, constraint_counter, vehicles, ind, val);
            ++constraint_counter;
        }

        // Constraint: Only one Assignment per Vehicle
        for (int j = 1; j <= vehicles; ++j) {
            String name = "Constr" + constraint_counter;
            GLPK.glp_set_row_name(lp, constraint_counter, name);
            GLPK.glp_set_row_bnds(lp, constraint_counter, GLPKConstants.GLP_DB, 0, 1);
            for (int i = 1; i <= requests; ++i) {
                GLPK.intArray_setitem(ind, i, vehicles * (i - 1) + j);
                GLPK.doubleArray_setitem(val, i, 1);
            }
            GLPK.glp_set_mat_row(lp, constraint_counter, requests, ind, val);
            ++constraint_counter;
        }
        assert constraint_counter - 1 == num_constraints;
        GLPK.delete_intArray(ind);
        GLPK.delete_doubleArray(val);
    }

    public void defineObjective() {
        GLPK.glp_set_obj_name(lp, "Assign");
        GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);
        int counter = 0;
        for (int i = 0; i < requests; i++) {
            for (int j = 0; j < vehicles; j++) {
                Scalar cij = cost_matrix.Get(i, j);
                Scalar pij = last_sol.Get(i, j);
                c_ij.set(cij, counter);
                p_ij.set(pij, counter);
                ++counter;
            }
        }
        for (int i = 1; i <= variables; i++) {
            /** Maximum Matching && Min. Cost */
            if (p_ij.Get(i - 1).number().doubleValue() == 1)
                GLPK.glp_set_obj_coef(lp, i, -1 * alpha - 1 * beta + gamma * c_ij.Get(i - 1).number().doubleValue());
            else
                GLPK.glp_set_obj_coef(lp, i, -1 * alpha + gamma * c_ij.Get(i - 1).number().doubleValue());
        }
    }

    public void solveProblem() {
        glp_smcp parm = new glp_smcp();
        GLPK.glp_init_smcp(parm);
        // int ret_val =
        GLPK.glp_simplex(lp, parm);
        obj_val = RealScalar.of(GLPK.glp_get_obj_val(lp));

        int var_counter = 1;
        for (int i = 0; i < requests; ++i)
            for (int j = 0; j < vehicles; ++j) {
                double prim = GLPK.glp_get_col_prim(lp, var_counter);
                last_sol.set(RealScalar.of(prim), i, j);
                ++var_counter;
            }
        // if (ret_val == 0) {
        // PrintSolution.of(lp);
        // GLPK.glp_write_lp(lp, null, "./target/test/Example");
        // } else {
        // System.out.println("The problem could not be solved");
        // // TODO Throw exception
        // }
    }

    public Scalar getObjectiveVal() {
        return obj_val; // FIXME Should not be a pointer only a value
    }

    // public void setObjectiveParams(double alpha, double beta, double gamma) {
    // this.alpha = alpha;
    // this.beta = beta;
    // this.gamma = gamma;
    // }

    public Tensor solveAdvanced(Tensor costMatrix, Tensor lastSol) {
        vehicles = Math.abs(costMatrix.get(0).length());
        requests = Math.abs(costMatrix.length());
        variables = this.requests * this.vehicles;
        this.cost_matrix = costMatrix;
        this.last_sol = lastSol;

        c_ij = Tensors.vector((i) -> RealScalar.ZERO, variables);
        p_ij = Tensors.vector((i) -> RealScalar.ZERO, variables);

        // For first run: If no previous solution exists, generate Zero-solution
        defineStructuralVariables(1);
        System.out.println("Structural variables defined");
        // }

        // Set up Problem
        defineConstraints();
        System.out.println("Assignment Constraints (Auxiliary Variables) set");

        // Add objective
        defineObjective();
        System.out.println("Objective set");

        solveProblem();
        GLPK.glp_delete_prob(lp);
        return last_sol;

    }

    public Tensor solve(Tensor costMatrix) {
        vehicles = Math.abs(costMatrix.get(0).length());
        requests = Math.abs(costMatrix.length());
        variables = this.requests * this.vehicles;
        this.cost_matrix = costMatrix;

        c_ij = Tensors.vector((i) -> RealScalar.ZERO, variables);
        p_ij = Tensors.vector((i) -> RealScalar.ZERO, variables);

        // For first run: If no previous solution exists, generate Zero-solution
        if (last_sol == null) {
            this.last_sol = Tensors.matrix((i, j) -> RealScalar.ZERO, requests, vehicles);
            defineStructuralVariables(1);
            System.out.println("Structural variables defined");
        }

        // Set up Problem
        defineConstraints();
        System.out.println("Assignment Constraints (Auxiliary Variables) set");

        // Add objective
        defineObjective();
        System.out.println("Objective set");

        solveProblem();
        GLPK.glp_delete_prob(lp);
        return last_sol;
    }
}
