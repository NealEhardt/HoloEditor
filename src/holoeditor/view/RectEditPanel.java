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
import javax.swing.*;

/**
 *
 * @author nehardt
 */
public class RectEditPanel extends JPanel
{
    final int H, R;
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
        H = editorService.getFrame().height;
        R = editorService.getFrame().radius;
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
            public void yChanged(int y) { }
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
        // Use R+1 to accommodate slider on right.
        boolean isPanelAspectWiderThanGridAspect = (R+1) * panelHeight
                                                  < H * panelWidth;
        float scale = isPanelAspectWiderThanGridAspect
            ? (panelHeight / H)
            : (panelWidth / (R+1));
        
        float gridOffX = getWidth() * padRatio;
        float gridOffY = getHeight() * padRatio;
        if (isPanelAspectWiderThanGridAspect) {
            gridOffX += (panelWidth - (R+1) * scale) / 2;
        } else {
            gridOffY += (panelHeight - H * scale) / 2;
        }
        gridToScreenTransform = AffineTransform.getTranslateInstance(gridOffX, gridOffY);
        gridToScreenTransform.scale(scale, scale);
        try {
            screenToGridTransform = gridToScreenTransform.createInverse();
        } catch (NoninvertibleTransformException ex) {
            // paintComponent() will fail gracefully
            gridToScreenTransform = null;
            screenToGridTransform = null;
        }
    }
    
    private void wireMouseEvents() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                Point cell = fromScreenToGrid(p);
                if (cell != null) {
                    dragColor = !slice[cell.y][cell.x];
                } else {
                    Float y = fromScreenToHandleTrack(p);
                    if (y != null) {
                        dragHandleY = y;
                    }
                }
                handleMouseEvent(p);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e.getPoint());
                dragColor = null;
                dragHandleY = null;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseEvent(e.getPoint());
            }
        });
    }
    
    private void handleMouseEvent(Point p) {
        if (dragColor != null) {
            Point cell = fromScreenToGrid(p);
            if (cell != null && dragColor != slice[cell.y][cell.x]) {
                slice[cell.y][cell.x] = dragColor;
                editorService.setRadialSlice(slice);
                repaint();
            }
        } else if (dragHandleY != null) {
            Float y = fromScreenToHandleTrack(p);
            if (y != null) {
                dragHandleY = y - 0.5f;
                editorService.setY(y.intValue());
                repaint();
            }
        }
    }
    
    /**
     * Converts mouse point on screen to cell coordinate in slice.
     * @param p mouse coordinate pair
     * @return int-rounded (x, y) coordinate pair
     */
    private Point fromScreenToGrid(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double x = result.getX();
        double y = result.getY();
        if (x >= 0 && y >= 0 && x < R && y < H) {
            return new Point((int)x, (int)y);
        }
        return null;
    }
    
    /**
     * Converts mouse point on screen to handle y.
     * @param p mouse coordinate pair
     * @return y position of handle [0, H)
     */
    private Float fromScreenToHandleTrack(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double x = result.getX();
        double y = result.getY();
        if ((x >= R && y >= 0 && x <= R+1 && y < H) || dragHandleY != null) {
            if (y < 0) {
                return 0f;
            } else if (y >= H) {
                return Math.nextDown(H);
            }
            return (float)y;
        }
        return null;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        if (gridToScreenTransform == null) {
            return; // sometimes the transform is noninvertible
        }
        
        // clear background
        g.setColor(Color.white);
        g2.fill(g.getClip());
        
        // paint children
        paintGrid(g2);
        paintHandle(g2);
    }
    
    void paintGrid(Graphics2D g) {
        // fill cells
        Rectangle2D rect = new Rectangle2D.Float();
        g.setColor(Color.black);
        for (int x = 0; x < R; x++) {
            for (int y = 0; y < H; y++) {
                if (!slice[y][x]) {
                    rect.setFrame(x, y, 1, 1);
                    g.fill(gridToScreenTransform.createTransformedShape(rect));
                }
            }
        }
        
        // vertical lines
        g.setColor(Color.lightGray);
        Line2D line = new Line2D.Float();
        for (int x = 0; x <= R; x++) {
            line.setLine(x, 0, x, H);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // horizontal lines
        for (int y = 0; y <= H; y++) {
            line.setLine(0, y, R, y);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
    }
    
    void paintHandle(Graphics2D g) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, .5);
        p.lineTo(1, 0);
        p.lineTo(1, 1);
        p.closePath();
        float y = dragHandleY==null ? editorService.getY() : dragHandleY;
        p.transform(AffineTransform.getTranslateInstance(R, y));
        p.transform(gridToScreenTransform);
        g.setColor(Color.red);
        g.fill(p);
        g.setColor(Color.black);
        g.draw(p);
    }
}
