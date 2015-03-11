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
public class CircleEditPanel extends JPanel
{
    final int C, R;
    boolean[][] slice;
    int y;
    EditorService editorService;
    
    final float padRatio = 0.1f;
    
     // grid coords go from (-R, -R) to (+R, +R)
    AffineTransform gridToScreenTransform;
    AffineTransform screenToGridTransform;
    
    Boolean dragColor = null;
    Float dragHandleTheta = null;
    
    public CircleEditPanel(EditorService editorService) {
        this.editorService = editorService;
        C = editorService.getFrame().circumference;
        R = editorService.getFrame().radius;
        slice = editorService.getCircularSlice();
        
        wireServiceEvents();
        wireComponentEvents();
        wireMouseEvents();
    }
    
    private void wireServiceEvents() {
        editorService.addListener(new EditorService.Listener() {
            @Override
            public void frameChanged() {
                if (dragColor == null) {
                    slice = editorService.getCircularSlice();
                    repaint();
                }
            }

            @Override
            public void thetaChanged(int theta) { }

            @Override
            public void yChanged(int y) {
                CircleEditPanel.this.y = y;
                slice = editorService.getCircularSlice();
                repaint();
            }
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
        // Use +2 to accommodate slider around border.
        float scale = panelHeight < panelWidth
            ? (panelHeight / (2*R + 2))
            : (panelWidth / (2*R + 2));
        
        float gridOffX = getWidth() / 2;
        float gridOffY = getHeight() / 2;
        gridToScreenTransform = AffineTransform.getTranslateInstance(gridOffX, gridOffY);
        gridToScreenTransform.scale(scale, -scale);
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
                    Float theta = fromScreenToHandleTrack(p);
                    if (theta != null) {
                        dragHandleTheta = theta;
                    }
                }
                handleMouseEvent(p);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e.getPoint());
                dragColor = null;
                dragHandleTheta = null;
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
                editorService.setCircularSlice(slice);
                repaint();
            }
        } else if (dragHandleTheta != null) {
            Float y = fromScreenToHandleTrack(p);
            if (y != null) {
                dragHandleTheta = y - 0.5f;
                editorService.setTheta(y.intValue());
                repaint();
            }
        }
    }
    
    /**
     * Converts mouse point on screen to cell coordinate in slice.
     * @param p mouse coordinate pair
     * @return int-rounded (x, theta) coordinate pair
     */
    private Point fromScreenToGrid(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double px = result.getX();
        double py = result.getY();
        double x = Math.sqrt(px*px + py*py);
        if (x < R) {
            double t = Math.atan2(py, px);
            if (t < 0) {
                t += 2*Math.PI;
            }
            double theta = t * C / (2*Math.PI);
            return new Point((int)x, (int)theta);
        }
        return null;
    }
    
    /**
     * Converts mouse point on screen to handle theta.
     * @param p mouse coordinate pair
     * @return theta of handle [0, C)
     */
    private Float fromScreenToHandleTrack(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double px = result.getX();
        double py = result.getY();
        double x = Math.sqrt(px*px + py*py);
        if ((x >= R && x < R+1) || dragHandleTheta != null) {
            double t = Math.atan2(py, px);
            if (t < 0) {
                t += 2*Math.PI;
            }
            double theta = t * C / (2*Math.PI);
            if (theta >= C) {
                theta = Math.nextDown(C);
            }
            return (float)theta;
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
        Arc2D arc = new Arc2D.Float(); // angles are 0-360 CW
        double angleExtent = 360.0 / C;
        for (int x = R-1; x >= 0; x--) {
            arc.setArcByCenter(0, 0, x+1, 0, angleExtent, Arc2D.PIE);
            for (int theta = 0; theta < C; theta++) {
                g.setColor(slice[theta][x] ? Color.white : Color.black);
                arc.setAngleStart((-theta - 1) * angleExtent);
                g.fill(gridToScreenTransform.createTransformedShape(arc));
            }
        }
        
        // radial lines
        g.setColor(Color.lightGray);
        Line2D line = new Line2D.Float();
        for (int theta = 0; theta < C/2; theta++) {
            double t = theta * 2*Math.PI / C;
            double x = R * Math.cos(t);
            double y = R * Math.sin(t);
            line.setLine(-x, -y, x, y);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // concentric circles
        Ellipse2D ellipse = new Ellipse2D.Float();
        for (int x = 1; x <= R; x++) {
            ellipse.setFrame(-x, -x, x*2, x*2);
            g.draw(gridToScreenTransform.createTransformedShape(ellipse));
        }
    }
    
    void paintHandle(Graphics2D g) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, 0);
        p.lineTo(1, -.5);
        p.lineTo(1, .5);
        p.closePath();
        float theta = dragHandleTheta==null ? editorService.getTheta() : dragHandleTheta;
        double t = (theta + 0.5) * 2*Math.PI / C;
        double x = R * Math.cos(t);
        double y = R * Math.sin(t);
        p.transform(AffineTransform.getRotateInstance(t));
        p.transform(AffineTransform.getTranslateInstance(x, y));
        p.transform(gridToScreenTransform);
        g.setColor(Color.red);
        g.fill(p);
        g.setColor(Color.black);
        g.draw(p);
    }
}
