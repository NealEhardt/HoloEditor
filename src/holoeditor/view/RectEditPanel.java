package holoeditor.view;

import holoeditor.model.*;
import holoeditor.model.Frame;
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
    int theta;
    Frame frame;
    EditorService editorService;
    
    final float padRatio = 0.05f;
    
     // grid coords span:
     // x [-R-1, +R)
     // y [0, H)
    AffineTransform gridToScreenTransform;
    AffineTransform screenToGridTransform;
    
    Brush brush;
    Double dragHandleY = null;
    
    public RectEditPanel(EditorService editorService, Brush brush) {
        this.editorService = editorService;
        this.brush = brush;
        H = holoeditor.model.Frame.Height;
        R = holoeditor.model.Frame.Radius;
        frame = editorService.getFrame();
        
        wireServiceEvents();
        wireMouseEvents();
    }
    
    private void wireServiceEvents() {
        editorService.addListener(new EditorService.Adapter() {
            @Override
            public void frameChanged() {
                repaint();
            }

            @Override
            public void thetaChanged(int theta) {
                RectEditPanel.this.theta = theta;
                repaint();
            }
        });
    }

    @Override
    public void doLayout() {
        updateTransforms();
    }
    
    private void updateTransforms() {
        float panelWidth = getWidth() * (1 - padRatio*2);
        float panelHeight = getHeight() * (1 - padRatio*2);
        float gridWidth = 2*R + EditorService.HandleSize;
        float gridHeight = H;
        float widthRatio = panelWidth / gridWidth;
        float heightRatio = panelHeight / gridHeight;
        boolean isPanelWiderThanGrid = heightRatio < widthRatio;
        float scale = isPanelWiderThanGrid ? heightRatio : widthRatio;
        
        float gridOffX = getWidth() / 2f;
        float gridOffY = getHeight() * padRatio;
        if (isPanelWiderThanGrid) {
            gridOffX += (panelWidth - gridWidth * scale) / 2;
        } else {
            gridOffY += (panelHeight - gridHeight * scale) / 2;
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
                PointTYR p = fromScreenToGrid(e.getPoint());
                if (p.r > R) {
                    dragHandleY = Math.max(0, Math.min(H-.01, p.y));
                    editorService.setY(dragHandleY.intValue());
                    repaint();
                } else if (isInSlice(p)) {
                    brush.begin(p);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                PointTYR p = fromScreenToGrid(e.getPoint());
                if (brush.isPainting()) {
                    brush.end(p);
                }
                dragHandleY = null;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
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
                if (dragHandleY != null) {
                    dragHandleY = Math.max(0, Math.min(H-.01, p.y));
                    editorService.setY(dragHandleY.intValue());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                repaint();
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
        double r = result.getX();
        double y = result.getY();
        return new PointTYR(theta + 0.5, y, r);
    }

    private boolean isInSlice(PointTYR p) {
        return p.y >= 0 && p.y < H && p.r >= -R && p.r <= R;
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
        Point p = getMousePosition();
        if (p != null && dragHandleY == null) {
            if (isInSlice(fromScreenToGrid(p)) || brush.isPainting()) {
                brush.paintPreview(g2, p, gridToScreenTransform.getScaleX());
            }
        }
        paintHandle(g2);
    }
    
    void paintGrid(Graphics2D g) {
        // fill cells
        Rectangle2D rect = new Rectangle2D.Float();
        g.setColor(Color.black);
        int farTheta = Math.floorMod(theta + Frame.Circumference/2,
                                     Frame.Circumference);
        for (int r = -R; r < R; r++) {
            for (int y = 0; y < H; y++) {
                boolean color;
                if (r < 0) {
                    int farR = -r - 1;
                    color = frame.data[farTheta][y][farR];
                } else {
                    color = frame.data[theta][y][r];
                }
                    
                if (!color) {
                    rect.setFrame(r, y, 1, 1);
                    g.fill(gridToScreenTransform.createTransformedShape(rect));
                }
            }
        }
        
        // vertical lines
        g.setColor(Color.lightGray);
        Line2D line = new Line2D.Float();
        for (int x = -R; x <= R; x++) {
            line.setLine(x, 0, x, H);
            g.setStroke(new BasicStroke(x == -R/2 || x == 0 || x == R/2 ? 3 : 1));
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
        
        // horizontal lines
        for (int y = 0; y <= H; y++) {
            line.setLine(-R, y, R, y);
            g.setStroke(new BasicStroke(y == H/4 || y == H/2 || y == 3*H/4 ? 3 : 1));
            g.draw(gridToScreenTransform.createTransformedShape(line));
        }
    }
    
    void paintHandle(Graphics2D g) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(0, 0);
        p.lineTo(1, -.5);
        p.lineTo(1, .5);
        p.closePath();
        float y = dragHandleY==null ? editorService.getY() + .5f
                    : dragHandleY.floatValue();

        p.transform(AffineTransform.getScaleInstance(EditorService.HandleSize,
                                                     EditorService.HandleSize));
        p.transform(AffineTransform.getTranslateInstance(R, y));
        p.transform(gridToScreenTransform);
        
        g.setColor(Color.red);
        g.fill(p);
        g.setColor(Color.black);
        g.draw(p);
    }
}
