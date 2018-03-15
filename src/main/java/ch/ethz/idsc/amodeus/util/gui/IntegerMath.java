/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.gui;

/* package */ enum IntegerMath {
    ;
    /** mod that behaves like in Matlab. for instance mod(-10, 3) == 2
     * 
     * @param index
     * @param size
     * @return matlab.mod(index, size) */
    public static int mod(int index, int size) {
        int value = index % size;
        // if value is below 0, then -size < value && value < 0.
        // For instance: -3%3==0, and -2%3==-2.
        return value < 0 ? size + value : value;
    }
}
