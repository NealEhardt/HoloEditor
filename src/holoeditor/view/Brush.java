/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.model.*;

/**
 *
 * @author nehardt
 */
public class Brush {
    double weight = 2;
    
    public interface Delegate {
        void setVoxel(PointTYR point, boolean color);
    }
    
    Delegate delegate;
    
    public Brush(Delegate delegate) {
        this.delegate = delegate;
    }
    
    boolean isPainting;
    public boolean isPainting() {
        return isPainting;
    }
    
    boolean color;
    public void setColor(boolean color) {
        this.color = color;
    }
    
    public void begin(PointTYR point) {
        isPainting = true;
        
        PointTYR iter = new PointTYR(Math.round(point.t),
                                    Math.round(point.y), Math.round(point.r));
        iterateT(iter, point);
    }
    
    void iterateT(PointTYR iter, PointTYR target) {
        while (iter.distanceTo(target) < weight) {
            iterateY(iter, target);
            iter.t++;
            if (iter.t % Frame.Circumference == Math.round(target.t)) break;
        }
        iter.t = Math.round(target.t) - 1;
        while (iter.distanceTo(target) < weight) {
            iterateY(iter, target);
            iter.t--;
            if (Math.floorMod((int)iter.t, Frame.Circumference) == Math.round(target.t)) break;
        }
        iter.t = Math.round(target.t);
    }
    
    void iterateY(PointTYR iter, PointTYR target) {
        while (iter.distanceTo(target) < weight) {
            iterateR(iter, target);
            iter.y++;
        }
        iter.y = Math.round(target.y) - 1;
        while (iter.distanceTo(target) < weight) {
            iterateR(iter, target);
            iter.y--;
        }
        iter.y = Math.round(target.y);
    }
    
    void iterateR(PointTYR iter, PointTYR target) {
        while (iter.distanceTo(target) < weight) {
            delegate.setVoxel(iter, color);
            iter.r++;
        }
        iter.r = Math.round(target.r) - 1;
        while (iter.distanceTo(target) < weight) {
            delegate.setVoxel(iter, color);
            iter.r--;
        }
        iter.r = Math.round(target.r);
    }
    
    public void move(PointTYR point) {
        begin(point); // TODO: interpolate
    }
    
    public void end(PointTYR point) {
        move(point);
        isPainting = false;
    }
}
