/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.*;
import java.util.ArrayList;

/**
 *
 * @author nehardt
 */
public class EditorService
{
    SerialService serialService;
    private Frame frame;
    private int theta;
    private int y;
    ArrayList<Listener> listeners = new ArrayList<>();
    
    public EditorService(SerialService serialService) {
        this.serialService = serialService;
        addListener(new Listener() {
            @Override
            public void frameChanged() {
                writePacket();
            }
            @Override
            public void thetaChanged(int theta) {
                writePacket();
            }
            @Override
            public void yChanged(int y) {
                writePacket();
            }
        });
        serialService.addListener(new SerialService.Adapter() {
            @Override
            public void connectedToPort(String portName) {
                writePacket();
                writePacket(); // first doesn't always take
            }
        });
    }
    
    private void writePacket() {
        int i = booleansToInt(frame.data[theta][y]);
        String packet = i+"\n";
        serialService.writePacket(packet);
    }
    
    int booleansToInt(boolean[] arr) {
        int n = 0;
        for (boolean b : arr)
            n = (n << 1) | (b ? 1 : 0);
        return n;
    }
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public Frame getFrame() {
        return frame;
    }
    public void setFrame(Frame frame) {
        this.frame = frame;
        for (Listener l : listeners) {
            l.frameChanged();
        }
    }
    
    public int getTheta() {
        return theta;
    }
    public void setTheta(int theta) {
        this.theta = theta;
        for (Listener l : listeners) {
            l.thetaChanged(theta);
        }
    }
    
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
        for (Listener l : listeners) {
            l.yChanged(y);
        }
    }
    
    public boolean[][] getRadialSlice() {
        return frame.getRadialSlice(theta);
    }
    public void setRadialSlice(boolean[][] slice) {
        frame.setRadialSlice(theta, slice);
        for (Listener l : listeners) {
            l.frameChanged();
        }
    }
    
    public boolean[][] getCircularSlice() {
        return frame.getCircularSlice(y);
    }
    public void setCircularSlice(boolean[][] slice) {
        frame.setCircularSlice(y, slice);
        for (Listener l : listeners) {
            l.frameChanged();
        }
    }
    
    public interface Listener {
        public void frameChanged();
        public void thetaChanged(int theta);
        public void yChanged(int y);
    }
}
