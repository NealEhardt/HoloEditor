/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.model.Frame;
import holoeditor.service.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author nehardt
 */
public class EditorJFrame extends JFrame
{
    EditorService editorService;
    DisplayService displayService;
    FileService fileService;

    EditorMenuBar menuBar;
    CircleEditPanel circlePanel;
    RectEditPanel rectPanel;
    
    public EditorJFrame(
            EditorService editorService,
            DisplayService displayService,
            FileService fileService) {
        this.editorService = editorService;
        this.displayService = displayService;
        this.fileService = fileService;
        
        initComponents();
        initMoreComponents();
        initFocusListener();
        
        fileService.addListener(new FileService.Adapter() {
            @Override
            public void fileChanged(File file) {
                setTitle(file.getName());
            }
        });
        
        setSize(700, 400);
    }
    
    private void initMoreComponents() {
        menuBar = new EditorMenuBar(editorService, fileService);
        setJMenuBar(menuBar);
        
        circlePanel = new CircleEditPanel(editorService);
        centerPanel.add(circlePanel);
        rectPanel = new RectEditPanel(editorService);
        centerPanel.add(rectPanel);
        
        statusLabel.setText("Starting...");
        
        displayService.addListener(new DisplayService.Listener() {
            @Override
            public void connected() {
                statusLabel.setText("Connected");
            }

            @Override
            public void disconnected() {
                statusLabel.setText("Disconnected");
            }
        });
    }
    
    private void initFocusListener() {
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                displayService.setFrame(editorService.getFrame());
            }
            @Override
            public void windowLostFocus(WindowEvent e) { }
        });
    }

    private void initComponents() {
        centerPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        reconnectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        centerPanel.setLayout(new java.awt.GridLayout(1, 2));
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        statusLabel.setText("status label");
        jPanel1.add(statusLabel, java.awt.BorderLayout.CENTER);

        reconnectButton.setText("Reconnect");
        reconnectButton.setEnabled(false);
        jPanel1.add(reconnectButton, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        pack();
    }

    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton reconnectButton;
    private javax.swing.JLabel statusLabel;
}
