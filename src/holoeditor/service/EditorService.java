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
    public static final float HandleSize = Frame.Radius * .1f;
    
    final DisplayService displayService;
    
    private Frame frame;
    private int theta;
    private int y;
    ArrayList<Listener> listeners = new ArrayList<>();
    
    public interface Listener {
        public void frameChanged();
        public void thetaChanged(int theta);
        public void yChanged(int y);
    }
    
    public EditorService(DisplayService displayService) {
        this.displayService = displayService;
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
        if (this.theta == theta) { return; }
        
        this.theta = theta;
        for (Listener l : listeners) {
            l.thetaChanged(theta);
        }
    }
    
    public int getY() {
        return y;
    }
    public void setY(int y) {
        if (this.y == y) { return; }
        
        this.y = y;
        for (Listener l : listeners) {
            l.yChanged(y);
        }
    }
    
    public void setVoxel(PointTYR point, boolean value) {
        this.setVoxel((int)point.t, (int)point.y, (int)point.r, value);
    }
    public void setVoxel(int t, int y, int r, boolean value) {
        if (r < 0) {
            r *= -1;
            t += Frame.Circumference / 2;
        }
        t = Math.floorMod(t, Frame.Circumference);
        if (y >= 0 && y < Frame.Height
                && r < Frame.Radius) {
            frame.setVoxel(Math.floorMod(t, Frame.Circumference), y, r, value);
            for (Listener l : listeners) {
                l.frameChanged();
            }
        }
    }
}
