/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.io;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.amodeus.util.math.UserHome;
import junit.framework.TestCase;

public class HttpDownloaderTest extends TestCase {
    public void testSimple() throws IOException {
        File file = UserHome.file("favicon.ico");
        assertFalse(file.exists());

        HttpDownloader.download("http://www.djtascha.de/favicon.ico", ContentType.IMAGE_XICON).to(file);
        assertTrue(file.isFile());

        file.delete();
    }

    public void testHttps() throws IOException {
        File file = UserHome.file("scenario.zip");
        assertFalse(file.exists());

        HttpDownloader.download("https://polybox.ethz.ch/index.php/s/AP9zPPk8wT4KWit/download", ContentType.APPLICATION_ZIP).to(file);

        assertTrue(file.isFile());
        // System.out.println(file.length());
        assertEquals(file.length(), 2284774);

        file.delete();
    }

    public void testFail() {
        File file = UserHome.file("scenario-does-not-exist.zip");
        assertFalse(file.exists());
        try {
            HttpDownloader.download( //
                    "https://polybox.ethz.ch/index.php/s/C3QUuk3cuWWS1Gmy/download123", //
                    ContentType.APPLICATION_ZIP).to(file);
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
        assertFalse(file.exists());
    }
}
