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
    
    private PointTYR previousPoint = null;
    boolean isPainting() {
        return previousPoint != null;
    }

    private double weight = 1;
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

    private int symmetries = 1;
    int getSymmetries() { return symmetries; }
    void setSymmetries(int symmetries) {
        this.symmetries = symmetries;
    }

    private boolean isMirror;
    void setMirror(boolean isMirror) {
        this.isMirror = isMirror;
    }

    private boolean color = true;
    boolean getColor() { return color; }
    void setColor(boolean color) {
        this.color = color;
    }

    void setVoxel(PointTYR point, boolean color) {
        delegate.setVoxel(point, color);

        double q = Frame.Circumference / (double)symmetries;
        for (int i = 1; i < symmetries; i++) {
            delegate.setVoxel(new PointTYR(point.t + i*q, point.y, point.r), color);
        }
        if (isMirror) {
            double u = -(point.t % q);
            for (int i = 0; i < symmetries; i++) {
                PointTYR p2 = new PointTYR(u + i * q, point.y, point.r);
                delegate.setVoxel(p2, color);
            }
        }
    }

    PointTYR[] getPreviewPoints(PointTYR mouse) {
        int length = symmetries * (isMirror ? 2 : 1);
        PointTYR[] points = new PointTYR[length];

        double q = Frame.Circumference / (double)symmetries;
        for (int i = 0; i < symmetries; i++) {
            points[i] = new PointTYR(mouse.t + i*q, mouse.y, mouse.r);
        }
        if (isMirror) {
            double u = -(mouse.t % q);
            for (int i = 0; i < symmetries; i++) {
                points[symmetries + i] = new PointTYR(u + i * q, mouse.y, mouse.r);
            }
        }
        return points;
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

    private void brushPoint(PointTYR point, Plane plane) {
        PointXYZ target = new PointXYZ(point); // XYZ for distance calculations.
        setVoxel(point, color); // Always color the voxel nearest to mouse.

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
    }

    void brushInterpolating(PointTYR point, Plane plane) {
        PointXYZ p = new PointXYZ(previousPoint);
        PointXYZ target = new PointXYZ(point);
        double dx = target.x - p.x;
        double dy = target.y - p.y;
        double dz = target.z - p.z;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        if (distanceSq > 1) {
            double dist = Math.sqrt(distanceSq);
            dx /= dist;
            dy /= dist;
            dz /= dist;
            for (int i = 1; i < dist; i++) {
                p.x += dx;
                p.y += dy;
                p.z += dz;
                brushPoint(new PointTYR(p), plane);
            }
        }

        brushPoint(point, plane);
    }
    
    public void begin(PointTYR point, Plane plane) {
        previousPoint = point;
        brushPoint(point, plane);

        delegate.commitChanges();
    }

    public void move(PointTYR point, Plane plane) {
        brushInterpolating(point, plane);
        previousPoint = point;

        delegate.commitChanges();
    }

    public void end(PointTYR point, Plane plane) {
        brushInterpolating(point, plane);
        previousPoint = null;

        delegate.commitChanges();
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
        setVoxel(iter, color);

        double startR = iter.r;
        iter.r++;
        while (iter.distanceSq(target) < weightSq) {
            setVoxel(iter, color);
            iter.r++;
        }
        iter.r = startR - 1;
        while (iter.distanceSq(target) < weightSq) {
            setVoxel(iter, color);
            iter.r--;
        }
        iter.r = startR;
    }
}
