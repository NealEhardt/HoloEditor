package holoeditor.model;

import javafx.geometry.Point3D;

/**
 *
 * @author nehardt
 */
public class PointTYR implements Cloneable {
    public double t, y, r;
    
    public PointTYR() {}
    public PointTYR(PointTYR point) {
        t = point.t;
        y = point.y;
        r = point.r;
    }
    public PointTYR(double t, double y, double r) {
        this.t = t;
        this.y = y;
        this.r = r;
    }
    
    public double distanceTo(PointTYR point) {
        double d = to3D().distance(point.to3D());
        return d;
    }
    
    public Point3D to3D() {
        double angle = t * 2*Math.PI / Frame.Circumference;
        double x = r * Math.cos(angle);
        double z = r * Math.sin(angle);
        return new Point3D(x, this.y, z);
    }
    
    @Override
    public String toString() {
        return "PointTYR[" + t + "," + y + "," + r + "]";
    }
}
