package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class SmallRedistributionProblemSolverTest {

    /** This test must pass because the fast solver is only given
     * 1 origin, therefore the shortest distance to any of the
     * destinations is a unique and globally optimal solution. */
    @Test
    public void test1() {
        Map<String, Integer> unitsToMove = new HashMap<>();
        unitsToMove.put("10", 1);

        Map<String, Integer> freeSpacesToGo = new HashMap<>();
        freeSpacesToGo.put("20", 3);
        freeSpacesToGo.put("30", 1);

        SmallRedistributionProblemSolver<String> fastSolver = //
                new SmallRedistributionProblemSolver<>(unitsToMove, freeSpacesToGo, //
                        (l1, l2) -> distance(l1, l2), l -> l, //
                        false, "");
        Assert.assertTrue(fastSolver.success());
    }

    
    
    /** A test encountered in simulations...*/
    @Test
    public void test2() {        
        Map<String, Integer> unitsToMove = new HashMap<>();
        unitsToMove.put("264806", 0);
        unitsToMove.put("415189", 0);
        unitsToMove.put("935015", 0);
        unitsToMove.put("646026", 1);

        Map<String, Integer> freeSpacesToGo = new HashMap<>();
        freeSpacesToGo.put("896621", 1);
        freeSpacesToGo.put("896625", 0);

        SmallRedistributionProblemSolver<String> fastSolver = //
                new SmallRedistributionProblemSolver<>(unitsToMove, freeSpacesToGo, //
                        (l1, l2) -> distance(l1, l2), l -> l, //
                        false, "");
        Assert.assertTrue(fastSolver.success());
    }
    
    private static final double distance(String s1, String s2) {
        String s3 = s1 + s2;
        return new Random(Long.parseLong(s3)).nextDouble();
    }

}
