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
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Neal
 */
public class SerialPortWorker extends SwingWorker<byte[], byte[]> {
    
    CommPortIdentifier portId;
    SerialPort port;

    InputStream input;
    OutputStream output;
    
    final Queue<byte[]> writeQueue = new ConcurrentLinkedQueue<>();

    final String VERIFY_STRING = "Hello! I'm an Arduino.";
    
    public SerialPortWorker(CommPortIdentifier portId) {
        this.portId = portId;
    }
    
    public void writePacket(byte[] packet) {
        writeQueue.add(packet);
    }

    @Override
    protected byte[] doInBackground() throws Exception {
        connect();
        while(true) { Thread.yield(); }
    }
        
    @Override
    protected void process(java.util.List<byte[]> chunks) {
        // pass messages out to external listeners
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
        port.notifyOnOutputEmpty(true);
        
        port.addEventListener((ev) -> serialEvent(ev));
    }
    
    private static SerialPort openSerialPort(CommPortIdentifier portID) throws IOException {
        SerialPort serialPort;
        try {
            serialPort = (SerialPort) portID.open("HoloEditor", 2000);
        } catch(PortInUseException ex) {
            throw new IOException("Serial port is already in use.");
        }
        try {
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Couldn't set serial port params.");
        }

        return serialPort;
    }

    private void verifyDevice() throws IOException {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] buffer = new byte[VERIFY_STRING.length()];

        int len = input.read(buffer);
        String in = new String(buffer, 0, len);

        if(!in.equals(VERIFY_STRING)) {
            throw new IOException("Hardware not found on this port.");
        }
    }
    
    private void serialEvent(SerialPortEvent ev) {
        int eventType = ev.getEventType();
        try {
            if (eventType == SerialPortEvent.DATA_AVAILABLE) {
                int readPacketSize = 16;
                if(input.available() >= readPacketSize) {
                    byte[] buffer = new byte[readPacketSize];
                    int len = input.read(buffer);
                    // TODO: send event to caller
                }
            } else if (eventType == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                if (!writeQueue.isEmpty()) {
                    output.write(writeQueue.poll());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
