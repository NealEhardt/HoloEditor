package holoeditor.service;

import holoeditor.model.*;
import java.util.HashSet;

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
    final private HashSet<Listener> listeners = new HashSet<>();

    public interface Listener {
        void frameChanged();
        void thetaChanged(int theta);
        void yChanged(int y);
        void colorChanged();
    }

    public static abstract class Adapter implements Listener {
        public void frameChanged() {}
        public void thetaChanged(int theta) {}
        public void yChanged(int y) {}
        public void colorChanged() {}
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
        frame.setVoxel(point, value);
    }

    public void commitChanges() {
        for (Listener l : listeners) {
            l.frameChanged();
        }
    }

    public void changeColor() {
        for (Listener l : listeners) {
            l.colorChanged();
        }
    }
}
