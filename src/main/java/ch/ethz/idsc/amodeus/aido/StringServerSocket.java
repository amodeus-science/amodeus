/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class StringServerSocket {
    public static final int DEFAULT_PORT = 9382;
    // ---
    ServerSocket serverSocket;
    Queue<StringClientSocket> queue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = true;

    /** [call in MATLAB] to start server listening on port
     * 
     * @param port */
    public StringServerSocket(int port, Function<Socket, ? extends StringClientSocket> function) {
        try {
            serverSocket = new ServerSocket(port);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            message("accept connections");
                            Socket socket = serverSocket.accept();
                            StringClientSocket mfileContainerSocket = function.apply((socket));
                            add(mfileContainerSocket);
                        } catch (Exception exception) {
                            if (isRunning) // prevent error if leaving
                                exception.printStackTrace();
                        }
                    }
                    message("thread stop");
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void add(StringClientSocket containerSocket) {
        queue.add(containerSocket);
        notify();
    }

    /** [call in MATLAB] to check if new connection is available
     * 
     * @return true, if new connection is available */
    public synchronized boolean hasSocket() {
        return !queue.isEmpty();
    }

    /** [call in MATLAB] to obtain earliest connection
     * 
     * @return mfile container socket */
    public synchronized StringClientSocket pollSocket() {
        return queue.poll();
    }

    public synchronized StringClientSocket getSocketWait() {
        if (queue.isEmpty())
            try {
                wait();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        return queue.poll();
    }

    /** [call in MATLAB] close this server */
    public void close() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        message("closed");
    }

    private void message(String message) {
        System.out.println(getClass().getSimpleName() + ": " + message);
    }
}
