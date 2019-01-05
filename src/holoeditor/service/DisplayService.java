/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.*;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Takes a Frame and rhythmically transmits its slices to a NetworkWorker.
 * 
 * @author Neal Ehardt
 */
public class DisplayService {
    boolean isConnected = false;
    final HashSet<Listener> listeners = new HashSet<Listener>();
    final LinkedBlockingDeque<Frame> frameQueue = new LinkedBlockingDeque<>();
    final Frame disconnectSignal = new Frame();
    
    public interface Listener {
        void connected();
        void disconnected();
    }
    
    public DisplayService() {}
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isConnected() { return isConnected; }
    
    public void start() {
        new Thread(() -> {
            try {
                while(true) {
                    try {
                        NetworkWorker worker = spawnWorker();
                        blockAndWriteFrames(worker);
                    } catch (IOException ex) {
                        System.err.println("network broke");
                        System.err.println(ex);
                    }
                    frameQueue.clear();
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start();
    }
    
    public void setFrame(Frame frame) {
        new Frame(frame);
        frameQueue.add(frame);
    }
    
    NetworkWorker spawnWorker() throws IOException {
        NetworkWorker worker = new NetworkWorker();
        worker.addPropertyChangeListener((evt) -> { // on Event Dispatch Thread
            switch (evt.getPropertyName()) {
                case "state":
                    switch ((SwingWorker.StateValue)evt.getNewValue()) {
                    case STARTED:
                        handleConnected();
                        break;
                    case DONE:
                        handleDisconnected();
                        break;
                    }
                    break;
                case "gotPacket":
                    String packet = (String)evt.getNewValue();
                    handlePacket(packet);
                    break;
            }
        });
        worker.execute();
        return worker;
    }
    
    void blockAndWriteFrames(NetworkWorker worker) throws IOException, InterruptedException {
        while (true) {
            // Clear the queue and get the last frame
            while (frameQueue.size() > 1) {
                Frame f = frameQueue.take();
                if (f == disconnectSignal) return;
            }
            Frame frame = frameQueue.take();
            if (frame == disconnectSignal) return;

            // Write status and frame
            OutputStream out = worker.outputStream;
            OutputStreamWriter writer = worker.writer;
            if (out != null && writer != null) {
                writer.write("Status: All Gravy\n");
                writer.flush();

                byte[] bytes = frame.getMatrixEncoding();
                out.write(bytes);
                out.flush();
            }
        }
    }
    
    void handleConnected() {
        isConnected = true;
        for (Listener i : listeners) {
            i.connected();
        }
    }
    
    void handlePacket(String packet) {
        System.out.println(">> " + packet); // dead for now
    }

    void handleDisconnected() {
        isConnected = false;
        frameQueue.add(disconnectSignal);
        for (Listener i : listeners) {
            i.disconnected();
        }
    }
}
