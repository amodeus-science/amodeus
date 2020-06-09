/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package amodeus.amodeus.util.net;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;

public final class ObjectClient implements AutoCloseable {
    public final String IP;
    private Socket socket;
    private ObjectOutputStream objectOutputStream = null;
    private volatile boolean isLaunched = true;

    public ObjectClient(final String IP, int port, ObjectHandler objectHandler) throws Exception {
        this.IP = IP;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    socket = new Socket(InetAddress.getByName(IP), port); // blocking if IP cannot be reached
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.flush();
                    // constructor blocks until the corresponding ObjectOutputStream has written and flushed the header
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    while (isLaunched) {
                        Object object = objectInputStream.readObject(); // blocks until object is available
                        objectHandler.handle(object);
                    }
                } catch (Exception myException) {
                    if (isLaunched)
                        myException.printStackTrace();

                }
                close();
            }
        }).start();
    }

    @Override // from AutoCloseable
    public void close() {
        isLaunched = false;
        if (Objects.nonNull(socket))
            try {
                socket.close();
                socket = null;
            } catch (Exception myException) {
                myException.printStackTrace();
            }
    }

    public boolean isOpen() {

        return Objects.nonNull(socket) && Objects.nonNull(objectOutputStream);
    }
}
