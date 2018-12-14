package holoeditor.model;

/**
 * Stores a 3D point in cartesian coordinates (x, y, z).
 * @author Neal Ehardt
 */
public class PointXYZ {
    public double x, y, z;

    public PointXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PointXYZ(PointTYR p) {
        double angle = p.t * 2*Math.PI / Frame.Circumference;
        x = p.r * Math.cos(angle);
        y = p.y;
        z = p.r * Math.sin(angle);
    }

    public double distance(PointXYZ p) {
        return Math.sqrt(distanceSq(p));
    }

    public double distanceSq(PointXYZ p) {
        double dx = x - p.x;
        double dy = y - p.y;
        double dz = z - p.z;
        return dx*dx + dy*dy + dz*dz;
    }

    @Override
    public String toString() {
        return "PointXYZ[" + x + "," + y + "," + z + "]";
    }
}
