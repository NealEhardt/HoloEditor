/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.service.EditorService;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author nehardt
 */
public class CircleEditPanel extends JPanel
{
    final int R, D;
    EditorService editorService;
    
    public CircleEditPanel(EditorService editorService) {
        this.editorService = editorService;
        R = editorService.getFrame().radius;
        D = editorService.getFrame().diameter;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0, 0, 500, 500);
    }
}
