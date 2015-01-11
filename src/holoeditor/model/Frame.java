/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.model;

/**
 *
 * @author nehardt
 */
public class Frame
{
    public final int radius, height, diameter;
    public final boolean[][][] data;
    
    public Frame(int radius, int height, int diameter) {
        this.radius = radius;
        this.height = height;
        this.diameter = diameter;
        data = new boolean[radius][height][diameter];
    }
    
    /**
     * 
     * @param theta
     * @return radius x height
     */
    public boolean[][] getRadialSlice(int theta) {
        boolean[][] result = new boolean[radius][height];
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < height; j++) {
                result[i][j] = data[i][j][theta];
            }
        }
        return result;
    }
    
    /**
     * 
     * @param theta
     * @param slice radius x height
     */
    public void setRadialSlice(int theta, boolean[][] slice) {
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j][theta] = slice[i][j];
            }
        }
    }
    
    /**
     * 
     * @param y
     * @return radius x theta
     */
    public boolean[][] getCircularSlice(int y) {
        boolean[][] result = new boolean[radius][];
        for (int i = 0; i < radius; i++) {
            result[i] = data[i][y].clone();
        }
        return result;
    }
    
    /**
     * 
     * @param y
     * @param slice radius x theta
     */
    public void setCircularSlice(int y, boolean[][] slice) {
        for (int i = 0; i < radius; i++) {
            data[i][y] = slice[i].clone();
        }
    }
}
