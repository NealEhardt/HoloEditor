package holoeditor.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author nehardt
 */
public class Frame implements Serializable
{
    private static final long serialVersionUID = 2L;
    
    public static final int Circumference = 128, Height = 24, Radius = 16;
    
    public static final int[] DivisionsByR = new int[Radius];
    static {
        int divisions = Circumference;
        for (int r = Radius-1; r >= 0; r--) {
            while (4*Math.PI*(r+.5) <= divisions) {
                divisions /= 2;
            }
            DivisionsByR[r] = divisions;
        }
    }
    
    /** theta [0, circumference) * y [0, height) * r [0, radius) */
    public final boolean[][][] data;
    
    /**
     * Creates a blank frame.
     */
    public Frame() {
        data = new boolean[Circumference][Height][Radius];
    }

    /**
     * Copies a frame.
     */
    public Frame(Frame frame) {
        data = new boolean[Circumference][Height][];
        for (int t = 0; t < Circumference; t++) {
            for (int y = 0; y < Height; y++) {
                data[t][y] = Arrays.copyOf(frame.data[t][y], Radius);
            }
        }
    }
    
    /**
     * 
     * @param t any value, will be modulo'd to [0, Circumference)
     * @param y in range [0, +Height)
     * @param r in range (-Radius, +Radius)
     */
    public void setVoxel(int t, int y, int r, boolean color) {
        if (r < 0) {
            r *= -1;
            t += Circumference / 2;
        }
        int tMod = Math.floorMod(t, Circumference);
        if (y >= 0 && y < Height && r < Radius) {
            data[tMod][y][r] = color;
        }
    }

    public void setVoxel(PointTYR point, boolean color) {
        boolean q = point.r < 0;
        setVoxel((int)Math.floor(q ? point.t + Circumference/2 : point.t),
                (int)point.y,
                (int)(q ? -point.r : point.r),
                color);
    }
    
    public boolean getVoxel(PointTYR point) {
        return getVoxel((int)point.t, (int)point.y, (int)point.r);
    }
    public boolean getVoxel(int t, int y, int r) {
        if (r < 0) {
            r *= -1;
            t += Circumference / 2;
        }
        t = Math.floorMod(t, Circumference);
        return data[t][y][r];
    }

    final int SLICE_SIZE = 2 * Radius * Height / 8; // * 1-bit color

    /**
     * Serializes this Frame as a series of slices. Each slice has a bit order
     * that can be written directly to the matrix.
     * @return frame for matrix
     */
    public byte[] getMatrixEncoding() {
        byte[] buffer = new byte[SLICE_SIZE * Circumference];
        for (int theta = 0; theta < Circumference; theta++) {
            byte[] slice = getMatrixEncodedSlice(theta);
            System.arraycopy(slice, 0,
                             buffer, theta * SLICE_SIZE,
                             SLICE_SIZE);
        }
        return buffer;
    }
    
    byte[] getMatrixEncodedSlice(int theta) {
        byte[] slice = new byte[SLICE_SIZE];

        int idx = 0;
        for (int register_row = Height / 4 - 1; register_row >= 0; register_row--) { // 5, 4, ...,0
            for (int byte_column = Radius - 1; byte_column >= 0; byte_column--) { // 15, 14, ...,0
                int t = theta;
                int r = 2 * byte_column - Radius + 1;
                if (byte_column < Radius / 2) {
                    t = (t + Circumference / 2) % Circumference;
                    r = -1 - r;
                }
                byte b = 0;
                for (int bit = 7; bit >= 0; bit--) {
                    int y = 4 * register_row + (bit % 4);
                    if (bit == 3) {
                        r += t == theta ? -1 : +1;
                    }
                    b <<= 1;
                    b |= (data[t][y][r] ? 1 : 0);
                }
                slice[idx++] = b;
            }
        }
        
        return slice;
    }
}
