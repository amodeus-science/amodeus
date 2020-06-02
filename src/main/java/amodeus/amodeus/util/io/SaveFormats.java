/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.io;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.CsvFormat;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Put;

public enum SaveFormats {
    MATHEMATICA {
        @Override
        public File save(Tensor tensor, File folder, String name) throws IOException {
            File file = new File(folder, name + ".mathematica");
            Put.of(file, tensor);
            return file;
        }
    },
    CSV {
        @Override
        public File save(Tensor tensor, File folder, String name) throws IOException {
            File file = new File(folder, name + ".csv");
            // CsvFormat.strict() formats entries of tensor if necessary
            Export.of(file, tensor.map(CsvFormat.strict()));
            return file;
        }
    },
    CSV_GZ { // imported as table in Mathematica using Import["filename.csv.gz"]
        @Override
        public File save(Tensor tensor, File folder, String name) throws IOException {
            File file = new File(folder, name + ".csv.gz");
            // CsvFormat.strict() formats entries of tensor if necessary
            Export.of(file, tensor.map(CsvFormat.strict()));
            return file;
        }
    },
    MATLAB {
        @Override
        public File save(Tensor tensor, File folder, String name) throws IOException {
            File file = new File(folder, name + ".m");
            Export.of(file, tensor);
            return file;
        }
    };

    public abstract File save(Tensor tensor, File folder, String name) throws IOException;

}
