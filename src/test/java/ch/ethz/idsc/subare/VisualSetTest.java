package ch.ethz.idsc.subare;

import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import junit.framework.TestCase;

public class VisualSetTest extends TestCase {

    public void testConstructors() {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor points = Transpose.of(Tensors.of(domain, values));

        VisualRow row1 = new VisualRow();
        VisualRow row2 = new VisualRow(domain, values);
        VisualRow row3 = new VisualRow(points);

        VisualSet set1 = new VisualSet();
        VisualSet set2 = new VisualSet(row1);
        VisualSet set3 = new VisualSet(row1, row2, row3);

        assertEquals(set1.visualRows().size(), 0);
        assertEquals(set2.visualRows().size(), 1);
        assertEquals(set3.visualRows().size(), 3);

        assertEquals(set3.get(2).getValues(), values);
    }

    public void testAdd() {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 5);

        VisualRow row1 = new VisualRow(domain, values1);
        VisualRow row2 = new VisualRow(domain, values2);

        VisualSet set = new VisualSet(row1);
        set.add(row2);

        assertEquals(set.get(1).getValues(), values2);
    }

    public void testSetRowLabel() {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 5);

        VisualRow row1 = new VisualRow(domain, values1);
        VisualRow row2 = new VisualRow(domain, values2);

        VisualSet set = new VisualSet(row1, row2);

        set.setRowLabel(0, "row 1");
        set.setRowLabel(1, "row 2");

        assertEquals(set.get(1).getLabelString(), "row 2");
    }

}
