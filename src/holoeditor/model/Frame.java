/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.model;

import java.io.Serializable;

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
    public final boolean[][][] data = new boolean[Circumference][Height][Radius];
    
    /**
     * Creates a blank frame.
     */
    public Frame() { }
    
    /**
     * 
     * @param y [0, height)
     * @return theta [0, circumference) * r [0, radius)
     */
    public boolean[][] getCircularSlice(int y) {
        boolean[][] slice = new boolean[Circumference][];
        for (int theta = 0; theta < Circumference; theta++) {
            slice[theta] = data[theta][y].clone();
        }
        return slice;
    }
    
    /**
     * 
     * @param t [0, circumference)
     * @param y [0, height)
     * @param r [0, radius)
     * @param color
     */
    public void setVoxel(int t, int y, int r, boolean color) {
        data[t][y][r] = color;
    }
    
    public void setVoxel(PointTYR point, boolean color) {
        setVoxel((int)point.t, (int)point.y, (int)point.r, color);
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
    
    public byte[] getPacket(int theta) {
        byte[] packet = new byte[Radius * 2];
        int i = 0;
        
        // near slice
        for (int r = Radius-1; r >= 0; r--) {
            byte column = 0;
            for (int y = 0; y < Height; y++) {
                boolean b = data[theta][y][r];
                column <<= 1;
                column |= (b ? 1 : 0);
            }
            packet[i++] = column;
        }
        
        // far slice
        int farTheta = (theta + Circumference/2) % Circumference;
        for (int r = 0; r < Radius; r++) {
            byte column = 0;
            for (int y = 0; y < Height; y++) {
                boolean b = data[farTheta][y][r];
                column <<= 1;
                column |= (b ? 1 : 0);
            }
            packet[i++] = column;
        }
        
        return packet;
    }
}
