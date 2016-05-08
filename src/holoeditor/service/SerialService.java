/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import gnu.io.CommPortIdentifier;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Collections;

/**
 * Reads and writes to Arduino over serial. Call tryNextPort to begin scanning.
 * Listen for scanning, connected, received, and disconnected events.
 * 
 * @author Neal Ehardt
 */
public class SerialService {
    
    Queue<CommPortIdentifier> portIds;
    SerialPortWorker worker;
    
    ArrayList<Listener> listeners = new ArrayList<>();
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public SerialService() { }
    
    public void tryNextPort() {
        // Disconnect
        if (worker != null) {
            boolean isNotifyListeners = worker.isConnected();
            String portName = worker.portId.getName();
            
            worker.disconnect();
            worker = null;
            
            if (isNotifyListeners) {
                for (Listener l : listeners) {
                    l.disconnectedFromPort(portName);
                }
            }
        }
        
        // Get next portId
        if (portIds == null || portIds.isEmpty()) {
            try {
                portIds = new ArrayDeque<>(Collections.list(CommPortIdentifier.getPortIdentifiers()));
            } catch (UnsatisfiedLinkError e) {
                System.err.println("Cannot find rxtxSerial binary.");
                return;
            }
        }
        CommPortIdentifier portId = (CommPortIdentifier)portIds.poll();
        String portName = portId.getName();
        for (Listener l : listeners) {
            l.scanningPort(portName);
        }
        
        // Start a worker
        worker = new SerialPortWorker(portId);
        worker.addPropertyChangeListener((evt) -> {
            String prop = evt.getPropertyName();
            switch (prop) {
                case "gotPacket":
                    String packet = (String)evt.getNewValue();
                    System.out.print(">> " + packet);
                    break;
                case "connected":
                    for (Listener l : listeners) {
                        l.connectedToPort(portName);
                    }
                    break;
            }
            if (worker.isDone()) {
                tryNextPort();
            }
        });
        worker.execute();
    }
    
    public void writePacket(String packet) {
        if (worker != null) {
            worker.writePacket(packet);
            System.out.print("<< " + packet);
        }
    }
    
    public interface Listener {
        void scanningPort(String portName);
        void connectedToPort(String portName);
        void disconnectedFromPort(String portName);
        void recievedPacket(byte[] packet);
    }
    
    public static class Adapter implements Listener {
        @Override
        public void scanningPort(String portName) { }
        @Override
        public void connectedToPort(String portName) { }
        @Override
        public void disconnectedFromPort(String portName) { }
        @Override
        public void recievedPacket(byte[] packet) { }
    }
}
