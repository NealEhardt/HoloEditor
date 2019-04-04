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
    public PointTYR(PointXYZ point) {
        t = Math.atan2(point.z, point.x) * Frame.Circumference / (2*Math.PI);
        y = point.y;
        r = Math.sqrt(point.x * point.x + point.z * point.z);
    }

    public double distance(PointXYZ p) {
        return new PointXYZ(this).distance(p);
    }

    public double distanceSq(PointXYZ p) {
        return new PointXYZ(this).distanceSq(p);
    }
    
    @Override
    public String toString() {
        return "PointTYR[" + t + "," + y + "," + r + "]";
    }
}
