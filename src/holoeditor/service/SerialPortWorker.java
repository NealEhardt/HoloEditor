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
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Neal
 */
public class SerialPortWorker extends SwingWorker<Void, String> {
    
    public final CommPortIdentifier portId;
    SerialPort port;

    InputStream input;
    OutputStream output;
    boolean isConnected;
    final String disconnectSignal = new String();
    
    final BlockingQueue<String> writeQueue = new LinkedBlockingQueue<>();
    
    public SerialPortWorker(CommPortIdentifier portId) {
        this.portId = portId;
    }
    
    public void writePacket(String packet) {
        writeQueue.add(packet);
    }
    
    public void disconnect() {
        writeQueue.clear();
        writeQueue.add(disconnectSignal);
    }
    
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    @SuppressWarnings("StringEquality")
    protected Void doInBackground() throws Exception {
        try {
            connect(); // big blocker
            while (output != null) {
                String packet = writeQueue.take(); // big blocker
                if (packet == disconnectSignal) {
                    break;
                }
                output.write(packet.getBytes());
            }
        } finally {
            isConnected = false;
            port.close();
        }
        return null;
    }
    
    @Override
    protected void done() {
        
    }
        
    @Override
    protected void process(java.util.List<String> chunks) {
        for (String packet : chunks) {
            firePropertyChange("gotPacket", null, packet);
        }
    }

    private void connect() throws Exception
    {
        if ( portId.isCurrentlyOwned() ) {
            throw new IOException("Port is currently in use");
        }

        port = openSerialPort(portId);

        input = port.getInputStream();
        output = port.getOutputStream();

        verifyDevice();

        port.notifyOnDataAvailable(true);
        
        port.addEventListener((ev) -> serialEvent(ev));
        
        isConnected = true;
        java.awt.EventQueue.invokeLater(() -> 
            firePropertyChange("connected", null, null));
    }
    
    private static SerialPort openSerialPort(CommPortIdentifier portID) throws IOException {
        SerialPort serialPort;
        try {
            serialPort = (SerialPort) portID.open("HoloEditor", 2000);
        } catch(PortInUseException ex) {
            throw new IOException("Serial port is already in use.");
        }
        try {
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Couldn't set serial port params.");
        }

        return serialPort;
    }

    private void verifyDevice() throws IOException {
        try {
            Thread.sleep(2000); // this is plenty of time for the first message
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
        }
        final String handshakeString = "Hello! I'm an Arduino.";
        byte[] buffer = new byte[handshakeString.length()];
        
        int len = input.read(buffer);
        String in = new String(buffer, 0, len);

        if(!in.equals(handshakeString)) {
            throw new IOException("Hardware not found on this port.");
        }
    }
    
    @SuppressWarnings({"PrimitiveArrayArgumentToVariableArgMethod"})
    private void serialEvent(SerialPortEvent ev) {
        int eventType = ev.getEventType();
        try {
            switch (eventType) {
                case SerialPortEvent.DATA_AVAILABLE:
                    byte[] buffer = new byte[input.available()];
                    int len = input.read(buffer, 0, buffer.length);
                    publish(new String(buffer, 0, len));
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
