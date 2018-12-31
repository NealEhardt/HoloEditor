package holoeditor.model;

//import javafx.geometry.PointXYZ;

/**
 * Stores a 3D point in cylindrical coordinates (theta, y, radius).
 * @author Neal Ehardt
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

    public double distance(PointTYR p) {
        return distance(new PointXYZ(p));
    }

    public double distance(PointXYZ p) {
        return new PointXYZ(this).distance(p);
    }
    
    @Override
    public String toString() {
        return "PointTYR[" + t + "," + y + "," + r + "]";
    }
}
