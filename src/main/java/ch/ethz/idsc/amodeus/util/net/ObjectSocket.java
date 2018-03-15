/* The code was developed by Jan Hakenberg in 2010-2014.
 * The file was released to public domain by the author. */

package ch.ethz.idsc.amodeus.util.net;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ObjectSocket implements AutoCloseable, ObjectHandler {
    private ObjectOutputStream objectOutputStream;
    private final Socket socket;
    private volatile boolean launched = true;

    public ObjectSocket(Socket socket) {
        this.socket = socket;
        try {
            // flush the stream immediately to ensure that constructors for receiving ObjectInputStreams will not block when reading the header
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // constructor blocks until the corresponding ObjectOutputStream has written and flushed the header
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                    while (launched) {
                        @SuppressWarnings("unused")
                        Object object = objectInputStream.readObject(); // blocking, might give EOFException
                        System.out.println("object received");
                    }
                } catch (EOFException eofException) {
                    System.out.println("client has disconnected");
                } catch (Exception exception) {
                    if (launched)
                        exception.printStackTrace();
                }
                launched = false;
                try {
                    close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        thread.start();
    }

    @Override // from AutoCloseable
    public void close() throws Exception {
        socket.close();
    }

    @Override // from ObjectHandler
    public void handle(Object object) {
        try {
            objectOutputStream.writeObject(object);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            System.out.println("unsubscribe");
            launched = false;
        }
    }

}
