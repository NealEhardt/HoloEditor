/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * Takes a Frame and rhythmically transmits its slices to a NetworkWorker.
 * 
 * @author Neal Ehardt
 */
public class DisplayService {
    Frame frame;
    volatile int sliceTheta;
    volatile long period = 2_000_000_000L;
    volatile long lastTick = System.nanoTime();
    HashSet<Listener> listeners = new HashSet<Listener>();
    
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
    
    public void start() {
        new Thread(() -> {
            try {
                while(true) {
                    try {
                        NetworkWorker worker = spawnWorker();
                        loopWithWorker(worker);
                    } catch (IOException ex) {
                        System.err.println("network broke");
                        System.err.println(ex);
                        for (Listener i : listeners) {
                            i.disconnected();
                        }
                    }
                    Thread.sleep(5000);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }).start();
    }
    
    public void setFrame(Frame frame) {
        this.frame = frame;
    }
    
    NetworkWorker spawnWorker() throws IOException {
        NetworkWorker worker = new NetworkWorker();
        worker.addPropertyChangeListener((evt) -> {
            String prop = evt.getPropertyName();
            switch (prop) {
                case "connected":
                    handleConnected();
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
    
    void loopWithWorker(NetworkWorker worker) throws IOException, InterruptedException {
        while (true) {
            worker.writeSlicePacket(frame.getPacket(sliceTheta % Frame.Circumference));
            sliceTheta++;

            long nextSliceTime = lastTick + period * sliceTheta / Frame.Circumference;
            long sleepTime = nextSliceTime - System.nanoTime();
            if (sleepTime > 0) {
                //System.out.println("sleep " + sleepTime);
                TimeUnit.NANOSECONDS.sleep(sleepTime);
            }
        }
    }
    
    void handleConnected() {
        for (Listener i : listeners) {
            i.connected();
        }
    }
    
    void handlePacket(String packet) {
        System.out.println("packet " + packet);
        if ("tick".equals(packet)) {
            long t = System.nanoTime();
            long newPeriod = t - lastTick;
            if (newPeriod > 10_000_000) {
                period = t - lastTick;
                lastTick = t;
                sliceTheta = 0;
                System.out.println("period = " + period);
            }
        }
    }
}
