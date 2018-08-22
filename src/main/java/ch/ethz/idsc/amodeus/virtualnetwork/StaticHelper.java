package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.function.Function;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Mean;

/* package */ enum StaticHelper {
    ;

    public static <T> Tensor meanOf(Collection<T> elements, Function<T, Tensor> locationOf) {
        Tensor col = Tensors.empty();
        elements.stream().forEach(t -> {
            col.append(locationOf.apply(t));
        });
        return Mean.of(col);
    }

    // public static void main(String[] args){
    // Collection<Coord> col = new ArrayList<>();
    // col.add(new Coord(1,0));
    // col.add(new Coord(1,1));
    // col.add(new Coord(0,1));
    // col.add(new Coord(0,0));
    //
    // System.out.println(meanOf(col, StaticHelper::ofCoord));
    // }
    //
    // public static Tensor ofCoord(Coord c) {
    // return Tensors.vector(c.getX(), c.getY());
    // }
}
