package amodeus.amodeus.dispatcher.alonso_mora_2016.rebalancing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iptcp;
import org.gnu.glpk.glp_prob;
import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraVehicle;
import amodeus.amodeus.dispatcher.alonso_mora_2016.TravelTimeCalculator;

public class RebalancingSolver {
    private final TravelTimeCalculator travelTimeCalculator;
    private final double now;

    public RebalancingSolver(TravelTimeCalculator travelTimeCalculator, double now) {
        this.travelTimeCalculator = travelTimeCalculator;
        this.now = now;
    }

    public Map<AlonsoMoraVehicle, Link> solve(List<AlonsoMoraVehicle> vehicles, List<Link> destinations) {
        double travelTimes[][] = new double[vehicles.size()][destinations.size()];

        for (int i = 0; i < vehicles.size(); i++) {
            AlonsoMoraVehicle vehicle = vehicles.get(i);

            for (int j = 0; j < destinations.size(); j++) {
                Link destination = destinations.get(j);
                travelTimes[i][j] = travelTimeCalculator.getTravelTime(now, vehicle.getLocation(), destination);
            }
        }

        int numberOfVariables = destinations.size() * vehicles.size();
        int numberOfAssignments = Math.min(destinations.size(), vehicles.size());

        if (numberOfVariables == 0) {
            return Collections.emptyMap();
        }

        // Start problem

        glp_prob problem = GLPK.glp_create_prob();
        GLPK.glp_set_prob_name(problem, "Rebalancing");

        // Add variables

        GLPK.glp_add_cols(problem, numberOfVariables);

        for (int i = 0; i < vehicles.size(); i++) {
            for (int j = 0; j < destinations.size(); j++) {
                GLPK.glp_set_col_kind(problem, i * destinations.size() + j + 1, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(problem, i * destinations.size() + j + 1, GLPKConstants.GLP_DB, 0.0, 1.0);
                GLPK.glp_set_col_name(problem, i * destinations.size() + j + 1, "y[" + "V" + i + ",L" + j + "]");
            }
        }

        // Add constraint

        GLPK.glp_add_rows(problem, 1);

        SWIGTYPE_p_int variables = GLPK.new_intArray(numberOfVariables + 1);
        SWIGTYPE_p_double values = GLPK.new_doubleArray(numberOfVariables + 1);

        for (int i = 0; i < numberOfVariables; i++) {
            GLPK.intArray_setitem(variables, i + 1, i + 1);
            GLPK.doubleArray_setitem(values, i + 1, 1.0);
        }

        GLPK.glp_set_row_bnds(problem, 1, GLPKConstants.GLP_FX, numberOfAssignments, numberOfAssignments);
        GLPK.glp_set_mat_row(problem, 1, numberOfVariables, variables, values);

        // Add objective

        GLPK.glp_set_obj_name(problem, "S");
        GLPK.glp_set_obj_dir(problem, GLPKConstants.GLP_MIN);

        for (int i = 0; i < vehicles.size(); i++) {
            for (int j = 0; j < destinations.size(); j++) {
                GLPK.glp_set_obj_coef(problem, i * destinations.size() + j + 1, travelTimes[i][j]);
            }
        }

        // Write problem

        // glp_cpxcp p = new glp_cpxcp();
        // GLPK.glp_write_lp(problem, p, "rebalancing_problem");

        // Solve problem

        glp_iptcp parameters = new glp_iptcp();
        GLPK.glp_init_iptcp(parameters);
        GLPK.glp_term_out(GLPK.GLP_OFF);

        int returnCode = GLPK.glp_interior(problem, parameters);
        Map<AlonsoMoraVehicle, Link> result = new HashMap<>();

        if (returnCode == 0) {
            int status = GLPK.glp_ipt_status(problem);

            if (status == GLPK.GLP_OPT) {
                // GLPK.glp_write_mip(problem, "rebalancing_solution");

                for (int i = 0; i < vehicles.size(); i++) {
                    for (int j = 0; j < destinations.size(); j++) {
                        if (GLPK.glp_ipt_col_prim(problem, i * destinations.size() + j + 1) > 0.5) {
                            result.put(vehicles.get(i), destinations.get(j));
                        }
                    }
                }
            }
        }

        GLPK.glp_delete_prob(problem);
        return result;
    }
}
