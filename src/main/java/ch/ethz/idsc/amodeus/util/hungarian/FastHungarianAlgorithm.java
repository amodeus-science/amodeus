package ch.ethz.idsc.amodeus.util.hungarian;

/* Copyright (c) 2018 ETHZ (Samuel Stauber)
 * Sources: 1) Hungarian Algorithm Copyright (c) 2012 Kevin L. Stern
 *          2) Subhash Suri, https://www.cs.ucsb.edu/~suri/cs231/Matching.pdf
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/

/** This program executes the Hungarian Algorithm in order to solve a bipartite
 * matching problem . An entry [i][j] of the double[n][m]-input-array represents
 * the cost of matching worker i to job j. An entry [i] of the int[n]-output-
 * array stores the best job j that was assigned to worker i. If there is no
 * job for a worker, i.e. j>i, the entry in the output-array will read -1.
 * 
 * The resulting matching will have minimum cost and therefore is an optimum.
 * All entries in the output array are unique.
 * 
 * @author Samuel J. Stauber */

public class FastHungarianAlgorithm {
    private final EqGraph eq;

    public FastHungarianAlgorithm(double[][] costMatrix, double eps) {
        eq = new EqGraph(costMatrix, eps);
    }

    public FastHungarianAlgorithm(double[][] costMatrix) {
        this(costMatrix, 1e-10);
    }

    public final int[] execute() {
        while (!eq.isSolved()) {
            int x = eq.pickFreeX();
            int y = eq.addS(x);
            eq.augmentMatching(x, y);
        }
        return eq.getResult();
    }

    /** call only after {@link #execute()}
     * 
     * @return */
    public final double getOptimalValue() {
        eq.saveOptimalValue();
        return eq.getOptimalValue();
    }

}
