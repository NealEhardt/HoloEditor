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
    
     // grid coords go from (0,0) to (W, H)
    AffineTransform gridToScreenTransform;
    AffineTransform screenToGridTransform;
    
    Boolean dragColor = null;
    Float dragHandleY = null;
    
    public RectEditPanel(EditorService editorService) {
        this.editorService = editorService;
        W = editorService.getFrame().radius;
        H = editorService.getFrame().height;
        slice = editorService.getRadialSlice();
        
        wireServiceEvents();
        wireComponentEvents();
        wireMouseEvents();
    }
    
    private void wireServiceEvents() {
        editorService.addListener(new EditorService.Listener() {
            @Override
            public void frameChanged() {
                if (dragColor == null) {
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
    
    private void wireComponentEvents() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTransforms();
            }
        });
    }
    
    private void updateTransforms() {
        float panelWidth = getWidth() * (1 - padRatio*2);
        float panelHeight = getHeight() * (1 - padRatio*2);
        // Avoid expensive divide operators; rearranged to use multiply instead.
        // Use W+1 to accommodate slider on right.
        boolean isPanelAspectWiderThanGridAspect = (W+1) * panelHeight
                                                  < panelWidth * H;
        float scale = isPanelAspectWiderThanGridAspect
            ? (panelHeight / H)
            : (panelWidth / (W+1));
        
        float gridOffX = getWidth() * padRatio;
        float gridOffY = getHeight() * padRatio;
        gridToScreenTransform = AffineTransform.getTranslateInstance(gridOffX, gridOffY);
        gridToScreenTransform.scale(scale, scale);
        try {
            screenToGridTransform = gridToScreenTransform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(RectEditPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void wireMouseEvents() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                Point cell = fromScreenToGrid(p);
                if (cell != null) {
                    dragColor = !slice[cell.x][cell.y];
                } else {
                    Float y = fromScreenToHandleTrack(p);
                    if (y != null) {
                        dragHandleY = y;
                    }
                }
                handleMousePoint(p);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMousePoint(e.getPoint());
                dragColor = null;
                dragHandleY = null;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMousePoint(e.getPoint());
            }
        });
    }
    
    private void handleMousePoint(Point p) {
        if (dragColor != null) {
            Point cell = fromScreenToGrid(p);
            if (cell != null && dragColor != slice[cell.x][cell.y]) {
                slice[cell.x][cell.y] = dragColor;
                editorService.setRadialSlice(slice);
                repaint();
            }
        } else if (dragHandleY != null) {
            Float y = fromScreenToHandleTrack(p);
            if (y != null) {
                dragHandleY = y - 0.5f;
                int yInt = (int)Math.floor(y);
                editorService.setY(yInt);
                repaint();
            }
        }
    }
    
    private Point fromScreenToGrid(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double x = result.getX();
        double y = result.getY();
        if (x >= 0 && y >= 0 && x < W && y < H) {
            return new Point((int)x, (int)y);
        }
        return null;
    }
    
    private Float fromScreenToHandleTrack(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double x = result.getX();
        double y = result.getY();
        if (x >= W && y >= 0 && x <= W+1 && y < H) {
            return (float)y;
        }
        return null;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        
        // clear background
        g.setColor(Color.white);
        g2.fill(g.getClip());
        
        // paint children
        paintGrid(g2);
        paintHandle(g2);
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
    
    void paintHandle(Graphics2D g) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, .5);
        p.lineTo(1, 0);
        p.lineTo(1, 1);
        p.closePath();
        float y = dragHandleY==null ? editorService.getY() : dragHandleY;
        p.transform(AffineTransform.getTranslateInstance(W, y));
        p.transform(gridToScreenTransform);
        g.setColor(Color.red);
        g.fill(p);
        g.setColor(Color.black);
        g.draw(p);
    }
}
