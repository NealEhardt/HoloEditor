/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.model.Frame;
import holoeditor.model.PointTYR;
import holoeditor.service.EditorService;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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
    int y;
    Frame frame;
    EditorService editorService;
    
    final float padRatio = 0.05f;
    
     // grid coords go from (-R, -R) to (+R, +R)
    AffineTransform gridToScreenTransform;
    AffineTransform screenToGridTransform;
    
    Brush brush = null;
    Double dragHandleTheta = null;
    
    public CircleEditPanel(EditorService editorService) {
        this.editorService = editorService;
        C = holoeditor.model.Frame.Circumference;
        R = holoeditor.model.Frame.Radius;
        frame = editorService.getFrame();
        
        wireServiceEvents();
        wireComponentEvents();
        wireMouseEvents();
    }
    
    private void wireServiceEvents() {
        editorService.addListener(new EditorService.Listener() {
            @Override
            public void frameChanged() {
                repaint();
            }

            @Override
            public void thetaChanged(int theta) { }

            @Override
            public void yChanged(int y) {
                CircleEditPanel.this.y = y;
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
        float gridR = 2*(R + EditorService.HandleSize);
        float scale = panelHeight < panelWidth
            ? panelHeight / gridR
            : panelWidth / gridR;
        
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
        brush = new Brush(new Brush.Delegate () {
            @Override
            public void setVoxel(PointTYR point, boolean color) {
                editorService.setVoxel(point, color);
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                PointTYR p = fromScreenToGrid(e.getPoint());
                if (p.r < R) {
                    brush.setColor(!frame.getVoxel(p));
                    brush.begin(p);
                } else {
                    dragHandleTheta = p.t;
                    editorService.setTheta(dragHandleTheta.intValue());
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                PointTYR p = fromScreenToGrid(e.getPoint());
                if (brush.isPainting()) {
                    brush.end(p);
                }
                dragHandleTheta = null;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                PointTYR p = fromScreenToGrid(e.getPoint());
                
                if (brush.isPainting()) {
                    brush.move(p);
                }
                if (dragHandleTheta != null) {
                    dragHandleTheta = p.t;
                    editorService.setTheta(dragHandleTheta.intValue());
                    repaint();
                }
            }
        });
    }
    
    /**
     * Converts mouse point on screen to cell coordinate in slice.
     * @param p mouse coordinate pair
     * @return
     */
    private PointTYR fromScreenToGrid(Point p) {
        Point2D result = screenToGridTransform.transform(p, null);
        double px = result.getX();
        double py = result.getY();
        double r = Math.sqrt(px*px + py*py);
        double angle = Math.atan2(py, px);
        if (angle < 0) {
            angle += 2*Math.PI;
        }
        double t = angle * C / (2*Math.PI);
        return new PointTYR(t, y, r);
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
        for (int r = R-1; r >= 0; r--) {
            arc.setArcByCenter(0, 0, r+1, 0, angleExtent, Arc2D.PIE);
            for (int theta = 0; theta < C; theta++) {
                g.setColor(frame.data[theta][y][r] ? Color.white : Color.black);
                arc.setAngleStart((-theta - 1) * angleExtent);
                g.fill(gridToScreenTransform.createTransformedShape(arc));
            }
        }
        
        // radial lines
        g.setColor(Color.lightGray);
        Line2D line = new Line2D.Float();
        for (int theta = 0; theta < C; theta++) {
            double t = theta * 2*Math.PI / C;
            
            int r = 0;
            while (theta % (C / holoeditor.model.Frame.DivisionsByR[r]) != 0) {
                r++;
            }
            double xUnit = Math.cos(t);
            double yUnit = Math.sin(t);
            line.setLine(r*xUnit, r*yUnit, R*xUnit, R*yUnit);
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // concentric circles
        Ellipse2D ellipse = new Ellipse2D.Float();
        for (int r = 1; r <= R; r++) {
            ellipse.setFrame(-r, -r, r*2, r*2);
            g.draw(gridToScreenTransform.createTransformedShape(ellipse));
        }
    }
    
    void paintHandle(Graphics2D g) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, 0);
        p.lineTo(1, -.5);
        p.lineTo(1, .5);
        p.closePath();
        
        p.transform(AffineTransform.getScaleInstance(EditorService.HandleSize,
                                                     EditorService.HandleSize));
        p.transform(AffineTransform.getTranslateInstance(R, 0));
        float theta = dragHandleTheta==null ? editorService.getTheta()
                        : dragHandleTheta.floatValue() - 0.5f;
        double t = (theta + 0.5) * 2*Math.PI / C;
        p.transform(AffineTransform.getRotateInstance(t));
        p.transform(gridToScreenTransform);
        
        g.setColor(Color.red);
        g.fill(p);
        g.setColor(Color.black);
        g.draw(p);
    }
}
