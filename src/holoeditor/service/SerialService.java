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
import java.util.Enumeration;
import java.util.Queue;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads and writes to Arduino over serial.
 * Automatically scans ports to find Arduino.
 * TODO: implement SwingWorker or similar background task pattern.
 * @author nehardt
 */
public class SerialService {

    SerialPort port;

    InputStream input;
    OutputStream output;
    
    Queue<byte[]> writeQueue;

    final String VERIFY_STRING = "Hello! I'm an Arduino.";
    
    public SerialService() {
        scanPorts(); // this is how we block
    }
    
    private void scanPorts() {
        while(true) {
            Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
            while(ports.hasMoreElements()) {
                CommPortIdentifier portId = (CommPortIdentifier)ports.nextElement();

                //PointFi.window.setStatus("Scanning for hardware ("+portId.getName()+")...");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    connectTo(portId);
                } catch(IOException e) {
                    //StringWriter sw = new StringWriter();
                    //e.printStackTrace(new PrintWriter(sw));
                    //PointFi.window.setStatus("Super awful error: " + sw.toString());
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public void connectTo(CommPortIdentifier portID) throws IOException
    {
        if ( portID.isCurrentlyOwned() ) {
            throw new IOException("Port is currently in use");
        }

        port = openSerialPort(portID);

        input = port.getInputStream();
        output = port.getOutputStream();

        verifyDevice();

        port.notifyOnDataAvailable(true);
        port.notifyOnOutputEmpty(true);
        
        try {
            port.addEventListener((ev) -> serialEvent(ev));
        } catch (TooManyListenersException ex) {
            throw new IOException("Too many listeners on serial port.");
        }
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
        
        switch(ev.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                int readPacketSize = 16;
                try {
                    if(input.available() >= readPacketSize) {
                        byte[] buffer = new byte[readPacketSize];
                        int len = input.read(buffer);
                        // TODO: send event to caller
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                try {
                    if (!writeQueue.isEmpty()) {
                        output.write(writeQueue.poll());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
    }
}
