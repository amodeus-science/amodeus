/* amod - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownloader {
    private static final int BUFFER_SIZE = 4096;

    public static HttpDownloader download(String fileURL, ContentType contentType) {
        return new HttpDownloader(fileURL, contentType);
    }

    // ---
    private final String fileURL;
    private final ContentType contentType;

    /** @param fileURL
     * @param contentType */
    private HttpDownloader(String fileURL, ContentType contentType) {
        this.fileURL = fileURL;
        this.contentType = contentType;
    }

    /** @param file
     * @throws IOException */
    public void to(File file) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        int responseCode = httpURLConnection.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // tests show that often: disposition == null
            String disposition = httpURLConnection.getHeaderField("Content-Disposition");
            String content_type = httpURLConnection.getContentType();
            if (contentType.matches(content_type)) {
                int contentLength = httpURLConnection.getContentLength();

                System.out.println("Content-Type = " + content_type);
                System.out.println("Content-Disposition = " + disposition);
                System.out.println("Content-Length = " + contentLength);

                byte[] buffer = new byte[BUFFER_SIZE];
                // opens input stream from the HTTP connection
                try (InputStream inputStream = httpURLConnection.getInputStream()) {
                    // opens an output stream to save into file
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        int bytesRead = -1;
                        while ((bytesRead = inputStream.read(buffer)) != -1)
                            outputStream.write(buffer, 0, bytesRead);

                    }
                }
            } else {
                httpURLConnection.disconnect();
                throw new RuntimeException(content_type);
            }
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpURLConnection.disconnect();
    }
}