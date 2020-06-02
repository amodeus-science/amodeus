/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package amodeus.amodeus.util.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Timer;

public abstract class AbstractServer {

    private volatile boolean isRunning = false;
    private ServerSocket serverSocket = null;
    private boolean waitForClients = true;

    protected abstract int getPort();

    protected abstract void handleConnection(Socket socket, Timer myTimer);

    public final void startAcceptingNonBlocking() {
        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = getPort();
                Timer timer = new Timer();
                try {
                    serverSocket = new ServerSocket(port);
                    System.out.println("server available...");
                    while (isRunning) {
                        Socket socket = serverSocket.accept();
                        handleConnection(socket, timer); // this blocks until socket connection or server is closed
                    }
                } catch (Exception exception) {
                    if (!exception.getMessage().equalsIgnoreCase("Socket closed"))
                        exception.printStackTrace();
                }
                timer.cancel();
            }
        }).start();
    }

    public final void setWaitForClients(boolean waitForClients) {
        this.waitForClients = waitForClients;
    }

    public final boolean getWaitForClients() {
        return isRunning && waitForClients;
    }

    /** closes server socket */
    public final void stopAccepting() {
        isRunning = false;
        if (Objects.nonNull(serverSocket))
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        System.out.println("server closed.");
    }

}
