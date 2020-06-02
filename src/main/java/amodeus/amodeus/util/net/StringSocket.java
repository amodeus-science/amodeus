/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class StringSocket implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader; //
    private volatile boolean launched = true;

    public StringSocket(final Socket socket) throws Exception {
        this.socket = socket;
        // flush the stream immediately to ensure that constructors for receiving ObjectInputStreams will not block when reading the header
        writer = new PrintWriter(socket.getOutputStream(), true);
        writer.flush();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public synchronized void writeln(Object object) throws Exception {
        writer.write(object.toString() + "\n");
        writer.flush();
    }

    public boolean isConnected() {
        return launched;
    }

    @Override
    public void close() {
        launched = false;
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readLine() throws Exception {
        return reader.readLine();
    }

}
