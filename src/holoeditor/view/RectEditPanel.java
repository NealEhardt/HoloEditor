/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.service.EditorService;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author nehardt
 */
public class RectEditPanel extends JPanel
{
    final int W, H;
    boolean[][] slice;
    int theta;
    EditorService editorService;
    
    final float padRatio = 0.1f;
    
     // grid goes from (0,0) to (W, H)
    AffineTransform gridToScreenTransform;
    AffineTransform screenToGridTransform;
    
    Boolean dragType = null;
    
    public RectEditPanel(EditorService editorService) {
        this.editorService = editorService;
        W = editorService.getFrame().radius;
        H = editorService.getFrame().height;
        slice = editorService.getRadialSlice();
        
        wireServiceEvents();
        wireMouseEvents();
    }
    
    private void wireServiceEvents() {
        editorService.addListener(new EditorService.Listener() {
            @Override
            public void frameChanged() {
                if (dragType == null) {
                    slice = editorService.getRadialSlice();
                    repaint();
                }
            }

            @Override
            public void thetaChanged(int theta) {
                RectEditPanel.this.theta = theta;
                slice = editorService.getRadialSlice();
                repaint();
            }

            @Override
            public void yChanged(int y) {} // I don't care!
        });
    }
    
    private void wireMouseEvents() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point cell = fromScreenToGrid(e.getPoint());
                if (cell != null) {
                    dragType = !slice[cell.x][cell.y];
                    affectWithMouseDrag(cell);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Point cell = fromScreenToGrid(e.getPoint());
                affectWithMouseDrag(cell);
                dragType = null;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point cell = fromScreenToGrid(e.getPoint());
                affectWithMouseDrag(cell);
            }
        });
    }
    
    private void affectWithMouseDrag(Point cell) {
        if (cell != null && dragType != slice[cell.x][cell.y]) {
            slice[cell.x][cell.y] = dragType;
            editorService.setRadialSlice(slice);
            repaint();
        }
    }
    
    private Point fromScreenToGrid(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double x = result.getX();
        double y = result.getY();
        if (x >= 0 && y >= 0 && x < W && y < W) {
            return new Point((int)x, (int)y);
        }
        return null;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int width = getWidth();
        int height = getHeight();
        
        // clear background
        g.setColor(Color.white);
        g2.fill(g.getClip());
        
        // grid
        float gridOffX = width * padRatio;
        float gridCellWidth = width * (1 - padRatio*2) / W;
        float gridOffY = height * padRatio;
        float gridCellHeight = height * (1 - padRatio*2) / H;
        gridToScreenTransform = AffineTransform.getTranslateInstance(gridOffX, gridOffY);
        gridToScreenTransform.scale(gridCellWidth, gridCellHeight);
        try {
            screenToGridTransform = gridToScreenTransform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(RectEditPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        paintGrid(g2);
    }
    
    void paintGrid(Graphics2D g) {
        g.setColor(Color.lightGray);
        Line2D line = new Line2D.Float();
        
        // vertical lines
        for (int x = 0; x <= W; x++) {
            line.setLine(x, 0, x, H);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // horizontal lines
        for (int y = 0; y <= H; y++) {
            line.setLine(0, y, W, y);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // fill cells
        Rectangle2D rect = new Rectangle2D.Float();
        g.setColor(Color.black);
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (slice[x][y]) {
                    rect.setFrame(x, y, 1, 1);
                    g.fill(gridToScreenTransform.createTransformedShape(rect));
                }
            }
        }
    }
}
