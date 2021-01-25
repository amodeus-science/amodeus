package amodeus.amodeus.dispatcher.alonso_mora_2016.ilp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;

import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraParameters;
import amodeus.amodeus.dispatcher.alonso_mora_2016.AlonsoMoraRequest;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.RequestTripEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rtv.RequestTripVehicleGraph.TripVehicleEdge;
import amodeus.amodeus.dispatcher.alonso_mora_2016.rv.RequestVehicleGraph;

public class ILPSolver {
    private final AlonsoMoraParameters parameters;

    public ILPSolver(AlonsoMoraParameters parameters) {
        this.parameters = parameters;
    }

    public Collection<TripVehicleEdge> solve(RequestTripVehicleGraph rtvGraph, RequestVehicleGraph rvGraph) {
        glp_prob problem = GLPK.glp_create_prob();
        GLPK.glp_set_prob_name(problem, "TripAssignment");

        Set<AlonsoMoraRequest> requests = rtvGraph.getRequestTripEdges().stream().map(e -> e.getRequest()).collect(Collectors.toSet());

        int numberOfRequests = requests.size();
        int numberOfEdges = rtvGraph.getTripVehicleEdges().size();
        int numberOfVehicles = rtvGraph.getVehicles().size();

        int numberOfConstraints = numberOfVehicles + numberOfRequests;
        int numberOfVariables = numberOfEdges + numberOfRequests;

        if (numberOfRequests == 0) {
            return Collections.emptySet();
        }

        // Add variables

        GLPK.glp_add_cols(problem, numberOfVariables);

        for (int i = 0; i < numberOfVariables; i++) {
            // TODO: Why not BV here ?
            GLPK.glp_set_col_kind(problem, i + 1, GLPKConstants.GLP_IV); // Integer Variable
            GLPK.glp_set_col_bnds(problem, i + 1, GLPKConstants.GLP_DB, 0, 1); // Between 0 and 1
        }

        for (int i = 0; i < numberOfEdges; i++) {
            TripVehicleEdge edge = rtvGraph.getTripVehicleEdges().get(i);
            GLPK.glp_set_col_name(problem, i + 1, String.format("e[T%d,V%d]", edge.getTripIndex(), edge.getVehicleIndex()));
        }

        for (int i = 0; i < numberOfRequests; i++) {
            GLPK.glp_set_col_name(problem, i + numberOfEdges + 1, "x" + i);
        }

        // Add constraints

        GLPK.glp_add_rows(problem, numberOfConstraints);

        // ... (1) one trip per vehicle

        List<List<Integer>> allVehicleIndices = new ArrayList<>(rtvGraph.getVehicles().size());

        for (int i = 0; i < rtvGraph.getVehicles().size(); i++) {
            allVehicleIndices.add(new LinkedList<>());
        }

        for (int i = 0; i < rtvGraph.getTripVehicleEdges().size(); i++) {
            TripVehicleEdge edge = rtvGraph.getTripVehicleEdges().get(i);
            allVehicleIndices.get(edge.getVehicleIndex()).add(i);
        }

        for (int i = 0; i < numberOfVehicles; i++) {
            List<Integer> vehicleIndices = allVehicleIndices.get(i);

            SWIGTYPE_p_int variables = GLPK.new_intArray(vehicleIndices.size() + 1);
            SWIGTYPE_p_double values = GLPK.new_doubleArray(vehicleIndices.size() + 1);

            for (int j = 0; j < vehicleIndices.size(); j++) {
                GLPK.intArray_setitem(variables, j + 1, vehicleIndices.get(j) + 1);
                GLPK.doubleArray_setitem(values, j + 1, 1.0);
            }

            GLPK.glp_set_row_bnds(problem, i + 1, GLPKConstants.GLP_UP, 0, 1);
            GLPK.glp_set_mat_row(problem, i + 1, vehicleIndices.size(), variables, values);
        }

        // ... (2) each request needs a vehicle or is unassigned

        List<AlonsoMoraRequest> requestList = new ArrayList<>(requests);

        for (int k = 0; k < numberOfRequests; k++) {
            List<RequestTripEdge> edges = rtvGraph.getRequestTripEdges(requestList.get(k));
            Set<Integer> requestIndices = new HashSet<>();

            // TODO: Not ideal all of this... we need to provide more performant lookup structures.
            // Best would be to have all vehicles/requests/trips by index

            for (RequestTripEdge edge : edges) {
                for (int edgeIndex = 0; edgeIndex < rtvGraph.getTripVehicleEdges().size(); edgeIndex++) {
                    if (rtvGraph.getTripVehicleEdges().get(edgeIndex).getTripIndex() == edge.getTripIndex()) {
                        requestIndices.add(edgeIndex);
                    }
                }
            }

            List<Integer> requestIndicesList = new ArrayList<>(requestIndices);

            SWIGTYPE_p_int variables = GLPK.new_intArray(requestIndices.size() + 2);
            SWIGTYPE_p_double values = GLPK.new_doubleArray(requestIndices.size() + 2);

            for (int j = 0; j < requestIndices.size(); j++) {
                GLPK.intArray_setitem(variables, j + 1, requestIndicesList.get(j) + 1);
                GLPK.doubleArray_setitem(values, j + 1, 1.0);
            }

            // Request selection variable is added at the end
            GLPK.intArray_setitem(variables, requestIndices.size() + 1, numberOfEdges + k + 1);
            GLPK.doubleArray_setitem(values, requestIndices.size() + 1, 1.0);

            GLPK.glp_set_row_bnds(problem, k + numberOfVehicles + 1, GLPKConstants.GLP_FX, 1, 1);
            GLPK.glp_set_mat_row(problem, k + numberOfVehicles + 1, requestIndices.size() + 1, variables, values);
        }

        // Add objective

        GLPK.glp_set_obj_name(problem, "C");
        GLPK.glp_set_obj_dir(problem, GLPKConstants.GLP_MIN);

        for (int i = 0; i < numberOfEdges; i++) {
            GLPK.glp_set_obj_coef(problem, i + 1, rtvGraph.getTripVehicleEdges().get(i).getCost());
        }

        for (int i = 0; i < numberOfRequests; i++) {
            GLPK.glp_set_obj_coef(problem, i + numberOfEdges + 1, parameters.unassignedPenalty);
        }

        // Write problem

        // glp_cpxcp p = new glp_cpxcp();
        // GLPK.glp_write_lp(problem, p, "problem");

        // Solve problem

        glp_iocp parameters = new glp_iocp();

        GLPK.glp_init_iocp(parameters);
        parameters.setPresolve(GLPK.GLP_ON);
        GLPK.glp_term_out(GLPK.GLP_OFF);

        int returnCode = GLPK.glp_intopt(problem, parameters);
        Collection<TripVehicleEdge> returnValue = new LinkedList<>();

        if (returnCode == 0) {
            int status = GLPK.glp_mip_status(problem);

            if (status == GLPK.GLP_OPT) {
                // GLPK.glp_write_mip(problem, "solution");

                for (int i = 0; i < numberOfEdges; i++) {
                    if (GLPK.glp_mip_col_val(problem, i + 1) > 0.0) {
                        returnValue.add(rtvGraph.getTripVehicleEdges().get(i));
                    }
                }
            }
        }

        GLPK.glp_delete_prob(problem);
        return returnValue;
    }
}
