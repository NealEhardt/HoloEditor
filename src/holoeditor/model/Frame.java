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
     * @param t any value, will be modulo'd to [0, Circumference)
     * @param y [0, +Height)
     * @param r (-Radius, +Radius)
     */
    public void setVoxel(int t, int y, int r, boolean color) {
        if (r < 0) {
            r *= -1;
            t += Circumference / 2;
        }
        t = Math.floorMod(t, Circumference);
        if (y >= 0 && y < Height && r < Radius) {
            data[t][y][r] = color;
        }
    }

    public void setVoxel(PointTYR point, boolean color) {
        boolean q = point.r < 0;
        setVoxel((int)(q ? point.t + Circumference/2 : point.t),
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
    
    public char[] getPacket(int theta) {
        char[] packet = new char[Radius * 2];
        int i = 0;
        
        // near slice
        for (int r = Radius-1; r >= 0; r--) {
            char column = 0;
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
            char column = 0;
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
