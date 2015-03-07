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
    public final int circumference, height, radius;
    
    /** theta [0, circumference) * y [0, height) * x [0, radius) */
    public final boolean[][][] data;
    
    /**
     * Creates a blank frame.
     * @param circumference
     * @param height
     * @param radius 
     */
    public Frame(int circumference, int height, int radius) {
        this.circumference = circumference;
        this.height = height;
        this.radius = radius;
        data = new boolean[circumference][height][radius];
    }
    
    /**
     * 
     * @param theta [0, circumference)
     * @return y [0, height) * x [0, radius)
     */
    public boolean[][] getRadialSlice(int theta) {
        boolean[][] slice = new boolean[height][radius];
        for (int i = 0; i < height; i++) {
            slice[i] = data[theta][i].clone();
        }
        return slice;
    }
    
    /**
     * 
     * @param theta [0, circumference)
     * @param slice y [0, height) * x [0, radius)
     */
    public void setRadialSlice(int theta, boolean[][] slice) {
        for (int i = 0; i < height; i++) {
            data[theta][i] = slice[i].clone();
        }
    }
    
    /**
     * 
     * @param y [0, height)
     * @return theta [0, circumference) * x [0, radius)
     */
    public boolean[][] getCircularSlice(int y) {
        boolean[][] slice = new boolean[circumference][];
        for (int theta = 0; theta < circumference; theta++) {
            slice[theta] = data[theta][y].clone();
        }
        return slice;
    }
    
    /**
     * 
     * @param y [0, height)
     * @param slice theta [0, circumference) * x [0, radius)
     */
    public void setCircularSlice(int y, boolean[][] slice) {
        for (int theta = 0; theta < circumference; theta++) {
            data[theta][y] = slice[theta].clone();
        }
    }
}
