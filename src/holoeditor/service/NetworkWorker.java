package holoeditor.service;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingWorker;

/**
 * Connects to the display by TCP. Call writeSlicePacket to send data.
 * Use addPropertyChangeListener for "connected" and "gotPacket" events.
 * 
 * See SwingWorker docs for more info about its lifecycle.
 * 
 * @author Neal
 */
public class NetworkWorker extends SwingWorker<Void, String> {
    final BlockingQueue<byte[]> writeQueue = new LinkedBlockingQueue<>();
    BufferedReader reader;
    OutputStreamWriter writer;
    
    public NetworkWorker() throws IOException {
        System.out.println("opening socket");
        Socket socket = new Socket("localhost", 8080);
        writer = new OutputStreamWriter(socket.getOutputStream());
        writeIntro(writer);
        
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        firePropertyChange("connected", null, null);
    }
    
    void writeIntro(OutputStreamWriter writer) throws IOException {
        writer.write("What's up Armadillo?\n");
        writer.flush();
        System.out.println("flushed");
    }
    
    public void writeSlicePacket(char[] packet) throws IOException {
        if (writer != null) {
            writer.write(packet);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (true) {
            String packet = reader.readLine();
            firePropertyChange("gotPacket", null, packet);
        }
    }
    
    @Override
    protected void done() {}
    
    @Override
    protected void process(java.util.List<String> chunks) {}
}
