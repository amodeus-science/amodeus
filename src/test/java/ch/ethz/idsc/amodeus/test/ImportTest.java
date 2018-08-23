/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.util.math.UserHome;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Import;
import junit.framework.TestCase;

/** gjoel noticed that on java9/windows Files::lines in an old implementation of
 * Import::of the file was not closed sufficiently fast to allow the deletion of
 * the file. */
public class ImportTest extends TestCase {
    public void testCsvClosed() throws IOException {
        File file = UserHome.file("tensorTest" + ImportTest.class.getSimpleName() + ".csv");
        assertFalse(file.exists());
        Export.of(file, Tensors.fromString("{{1, 2}, {3, 4}}"));
        assertTrue(file.exists());
        Tensor in = Import.of(file);
        boolean deleted = file.delete();
        assertTrue(deleted);
        assertEquals(Dimensions.of(in), Arrays.asList(2, 2));
    }

    public void testImageClose() throws Exception {
        Tensor tensor = Tensors.fromString("{{1, 2}, {3, 4}}");
        File file = UserHome.file("tensorTest" + ImportTest.class.getSimpleName() + ".png");
        Export.of(file, tensor);
        assertTrue(file.exists());
        Tensor in = Import.of(file);
        file.delete();
        assertFalse(file.exists());
        assertEquals(Dimensions.of(in), Arrays.asList(2, 2));
    }

    public void testFolderCsvClosed() throws IOException {
        File dir = UserHome.file("tensorTest" + System.currentTimeMillis());
        assertFalse(dir.exists());
        dir.mkdir();
        assertTrue(dir.isDirectory());
        File file = new File(dir, "tensorTest" + ImportTest.class.getSimpleName() + ".csv");
        assertFalse(file.exists());
        Export.of(file, Tensors.fromString("{{1, 2}, {3, 4}, {5, 6}}"));
        assertTrue(file.exists());
        Tensor table = Import.of(file);
        assertTrue(file.delete());
        assertTrue(dir.delete());
        assertEquals(Dimensions.of(table), Arrays.asList(3, 2));
    }

}
