package holoeditor.service;

import java.io.*;
import java.net.Socket;
import javax.swing.SwingWorker;

/**
 * Constructor connects to the display by TCP on caller's thread.
 * Reads lines from the network on its background thread.
 * Use addPropertyChangeListener for "state", and "gotPacket" events.
 * Events are received on the Event Dispatch Thread, so they are safe for Swing components.
 * 
 * Lifecycle details --
 * https://docs.oracle.com/javase/10/docs/api/javax/swing/SwingWorker.html
 * 
 * @author Neal
 */
public class NetworkWorker extends SwingWorker<Void, String> {
    BufferedReader reader;
    OutputStream outputStream;
    OutputStreamWriter writer;

    /**
     * Connects to the display by TCP on caller's thread.
     */
    public NetworkWorker() throws IOException {
        String host = "glados.mshome.net";
        int port = 52137;
        System.out.println("Connecting to " + host + ":" + port + ".");
        Socket socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
        writer = new OutputStreamWriter(outputStream);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        doHandshake(writer, reader);
    }
    
    void doHandshake(OutputStreamWriter writer, BufferedReader reader) throws IOException {
        writer.write("What's up Armadillo?\n");
        writer.flush();

        String response = reader.readLine();
        System.out.println(">> " + response); // TODO: validate?
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (true) {
            String packet = reader.readLine();
            if (packet == null) { // stream is closed, y'all
                return null;
            }
            firePropertyChange("gotPacket", null, packet);
        }
    }
    
    @Override
    protected void done() {}
    
    @Override
    protected void process(java.util.List<String> chunks) {}
}
