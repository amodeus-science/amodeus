package ch.ethz.idsc.subare;

import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import junit.framework.TestCase;

public class VisualRowTest extends TestCase {

    public void testConstructors() {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor points = Transpose.of(Tensors.of(domain, values));

        // VisualRow row1 = new VisualRow();
        VisualRow row2 = new VisualRow(domain, values);
        VisualRow row3 = new VisualRow(points);

        // assertTrue(row1.getDomain().equals(Tensors.empty()) && row1.getValues().equals(Tensors.empty()));
        assertTrue(row2.points().equals(points));
        assertTrue(row3.points().equals(points));
    }

    public void testAdd() {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values = RandomVariate.of(UniformDistribution.unit(), 5);

        VisualRow row = new VisualRow(domain, values);

        Tensor domainNext = Tensors.fromString("{6, 7}");
        Tensor valuesNext = RandomVariate.of(UniformDistribution.unit(), 2);
        // domain = Join.of(domain, domainNext);
        // values = Join.of(values, valuesNext);
        //// row.add(domainNext, valuesNext);
        // assertTrue(row.getDomain().equals(domain) && row.getValues().equals(values));
        //
        // domain.append(RealScalar.of(8));
        // values.append(RandomVariate.of(UniformDistribution.unit()));
        //// row.add(Tensors.of(domain.Get(7), values.Get(7)));
        // assertTrue(row.getDomain().equals(domain) && row.getValues().equals(values));
        //
        // domainNext = Tensors.fromString("{9, 10}");
        // valuesNext = RandomVariate.of(UniformDistribution.unit(), 2);
        // Tensor pointsNext = Transpose.of(Tensors.of(domainNext, valuesNext));
        // domain = Join.of(domain, domainNext);
        // values = Join.of(values, valuesNext);
        //// row.add(pointsNext);
        // assertTrue(row.getDomain().equals(domain) && row.getValues().equals(values));
        //
        // domain.append(RealScalar.of(11));
        // values.append(RandomVariate.of(UniformDistribution.unit()));
        //// row.add(Tensors.of(domain.Get(10), values.Get(10)));
        // assertTrue(row.getDomain().equals(domain) && row.getValues().equals(values));
    }
}
