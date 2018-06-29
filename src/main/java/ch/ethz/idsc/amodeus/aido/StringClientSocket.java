/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class StringClientSocket {
    final Socket socket;
    private PrintWriter writer;
    private BufferedReader reader = null; //
    private volatile boolean launched = true;

    public StringClientSocket(final Socket socket) {
        this.socket = socket;
        try {
            // flush the stream immediately to ensure that constructors for receiving ObjectInputStreams will not block when reading the header
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception myException) {
            myException.printStackTrace();
        }

        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (launched) {
                        Thread.sleep(1000);
                        // String line = reader.readLine();
                        // handle(line);
                    }
                } catch (Exception myException) {
                    if (launched) {
                        message("stop read");
                        // myException.printStackTrace();
                    }
                }
                launched = false;
            }
        });
        myThread.start();
    }

    public synchronized void writeln(Object line) throws Exception {
        writer.write(line + "\n");
        writer.flush();
    }

    public boolean isConnected() {
        return launched;
    }

    public void close() {
        launched = false;
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void message(String message) {
        System.out.println(getClass().getSimpleName() + ": " + message);
    }

    public String readLine() throws Exception {
        return reader.readLine();
    }

}
