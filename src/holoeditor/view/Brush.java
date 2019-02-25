package holoeditor.view;

import holoeditor.model.*;
import holoeditor.model.Frame;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Paints sphere around target point, via Delegate.
 * Over the lifecycle of a mouse interaction, call:
 *   mousePressed: brush.begin()
 *   mouseDragged: brush.move()
 *   mouseReleased: brush.end()
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
    
    private boolean isPainting;
    boolean isPainting() {
        return isPainting;
    }

    private double weight = 2;
    private double weightSq = weight * weight;
    void setWeight(double weight) {
        this.weight = weight;
        weightSq = weight * weight;
    }

    public enum Shape { Circle, Sphere }
    private Shape shape  = Shape.Circle;
    void setShape(Shape shape) {
        this.shape = shape;
    }

    private boolean color = true;
    boolean getColor() { return color; }
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

        if (shape == Shape.Sphere) {
            g.draw(new Ellipse2D.Double(x + r/2, y, r, 2 * r));
            g.draw(new Ellipse2D.Double(x, y + r/2, 2 * r, r));
        }
    }

    double roundHalf(double v) {
        return Math.floor(v) + 0.5;
    }

    public enum Plane { TR, YR }
    
    public void begin(PointTYR point, Plane plane) {
        isPainting = true;

        PointXYZ target = new PointXYZ(point);
        delegate.setVoxel(point, color); // always color the voxel nearest to mouse
        
        PointTYR iter = new PointTYR(
                roundHalf(point.t), roundHalf(point.y), roundHalf(point.r));

        switch (shape) {
            case Circle:
                switch (plane) {
                    case TR:
                        for (iter.t = 0.5; iter.t < Frame.Circumference; iter.t++) {
                            if (iter.distanceSq(target) < weightSq) {
                                iterateR(iter, target);
                            }
                        }
                        break;
                    case YR:
                        iterateY(iter, target);
                }
                break;
            case Sphere:
                for (iter.t = 0.5; iter.t < Frame.Circumference; iter.t++) {
                    iterateY(iter, target);
                }
        }

        delegate.commitChanges();
    }

    public void move(PointTYR point, Plane plane) {
        begin(point, plane); // TODO: interpolate
    }

    public void end(PointTYR point, Plane plane) {
        move(point, plane);
        isPainting = false;
    }

    void iterateY(PointTYR iter, PointXYZ target) {
        if (iter.distanceSq(target) < weightSq) {
            iterateR(iter, target);

            double startY = iter.y;
            iter.y++;
            while (iter.distanceSq(target) < weightSq) {
                iterateR(iter, target);
                iter.y++;
            }
            iter.y = startY - 1;
            while (iter.distanceSq(target) < weightSq) {
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
        while (iter.distanceSq(target) < weightSq) {
            delegate.setVoxel(iter, color);
            iter.r++;
        }
        iter.r = startR - 1;
        while (iter.distanceSq(target) < weightSq) {
            delegate.setVoxel(iter, color);
            iter.r--;
        }
        iter.r = startR;
    }
}
