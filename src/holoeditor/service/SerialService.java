/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and writes to Arduino over serial.
 * Automatically scans ports to find Arduino.
 * @author nehardt
 */
public class SerialService {
    
    SerialPortWorker worker;
    
    ArrayList<Listener> listeners = new ArrayList<>();
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public SerialService() {
        
    }
    
    public void scanPorts() {
        while(true) {
            Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
            while(ports.hasMoreElements()) {
                CommPortIdentifier portId = (CommPortIdentifier)ports.nextElement();
                worker = new SerialPortWorker(portId);
                worker.addPropertyChangeListener((evt) -> {
                    System.out.println(evt.getPropertyName() + " = " + evt.getNewValue());
                });
                worker.execute();
                
                for (Listener l : listeners) {
                    l.connectedToPort(portId.getName());
                }
            }
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
