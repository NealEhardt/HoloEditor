package holoeditor.view;

import holoeditor.model.*;
import holoeditor.model.Frame;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Paints sphere around target point, via Delegate.
 * Over the lifecycle of a mouse interaction, call:
 *   mousePressed: brush.begin
 *   mouseDragged: brush.move
 *   mouseReleased: brush.end
 * @author Neal Ehardt
 */
public class Brush {
    public interface Delegate {
        void setVoxel(PointTYR point, boolean color);
        void commitChanges();
    }
    
    Delegate delegate;
    
    public Brush(Delegate delegate) {
        this.delegate = delegate;
    }
    
    boolean isPainting;
    boolean isPainting() {
        return isPainting;
    }

    private double weight = 2;
    void setWeight(double weight) { this.weight = weight; System.out.println("weight " + weight); }
    
    boolean color;
    void setColor(boolean color) {
        this.color = color;
    }

    void paintPreview(Graphics2D g, Point mousePosition, double scale) {
        if (mousePosition == null) return;

        double r = weight * scale;
        double x = mousePosition.x - r;
        double y = mousePosition.y - r;

        g.setColor(Color.orange);
        g.draw(new Ellipse2D.Double(x, y, 2 * r, 2 * r));
    }

    double roundHalf(double v) {
        return Math.floor(v) + 0.5;
    }
    
    public void begin(PointTYR point) {
        isPainting = true;
        
        PointTYR iter = new PointTYR(0.5, roundHalf(point.y), roundHalf(point.r));
        PointXYZ target = new PointXYZ(point);
        for (iter.t = 0.5; iter.t < Frame.Circumference; iter.t++) {
            iterateY(iter, target);
        }

        delegate.commitChanges();
    }

    public void move(PointTYR point) {
        begin(point); // TODO: interpolate
    }

    public void end(PointTYR point) {
        move(point);
        isPainting = false;
    }

    void iterateY(PointTYR iter, PointXYZ target) {
        if (iter.distance(target) < weight) {
            iterateR(iter, target);

            double startY = iter.y;
            iter.y++;
            while (iter.distance(target) < weight) {
                iterateR(iter, target);
                iter.y++;
            }
            iter.y = startY - 1;
            while (iter.distance(target) < weight) {
                iterateR(iter, target);
                iter.y--;
            }
            iter.y = startY;
        }
    }
    
    void iterateR(PointTYR iter, PointXYZ target) {
        delegate.setVoxel(iter, color);

        double startR = iter.r;
        iter.r++;
        while (iter.distance(target) < weight) {
            delegate.setVoxel(iter, color);
            iter.r++;
        }
        iter.r = startR - 1;
        while (iter.distance(target) < weight) {
            delegate.setVoxel(iter, color);
            iter.r--;
        }
        iter.r = startR;
    }
}
