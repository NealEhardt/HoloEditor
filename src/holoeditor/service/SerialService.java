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
 * Reads and writes to Arduino over serial.
 * Automatically scans ports to find Arduino.
 * @author nehardt
 */
public class SerialService {
    
    Queue<CommPortIdentifier> ports;
    SerialPortWorker worker;
    
    ArrayList<Listener> listeners = new ArrayList<>();
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public SerialService() {
        tryNextPort();
    }
    
    private void tryNextPort() {
        if (ports == null || ports.isEmpty()) {
            ports = new ArrayDeque<>(Collections.list(CommPortIdentifier.getPortIdentifiers()));
        }
        CommPortIdentifier portId = (CommPortIdentifier)ports.poll();
        
        System.out.println("Connecting to "+portId.getName());
        worker = new SerialPortWorker(portId);
        worker.addPropertyChangeListener((evt) -> {
            if (worker.isDone()) {
                tryNextPort();
            }
        });
        worker.execute();

        for (Listener l : listeners) {
            l.connectedToPort(portId.getName());
        }
    }
    
    public void sendPacket(byte[] packet) {
        if (worker != null) {
            worker.writePacket(packet);
        }
    }
    
    public interface Listener {
        void connectedToPort(String portName);
        void disconnectedFromPort(String portName);
        void recievedPacket(byte[] packet);
    }
}
