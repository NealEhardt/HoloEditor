/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.service.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author nehardt
 */
public class EditorJFrame extends javax.swing.JFrame
{
    CircleEditPanel circlePanel;
    RectEditPanel rectPanel;
    
    EditorService editorService;

    public EditorJFrame(EditorService editorService) {
        super("HoloEditor"); // set frame title
        
        this.editorService = editorService;
        
        Container pane = getContentPane();
        circlePanel = new CircleEditPanel(editorService);
        pane.add(circlePanel);
        rectPanel = new RectEditPanel(editorService);
        pane.add(rectPanel);
        
        initComponents();
        
        setSize(700, 400);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout(1, 2));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}