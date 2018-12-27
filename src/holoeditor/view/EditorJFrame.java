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
import javax.swing.*;

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
        initListeners();

        setSize(700, 400);
    }
    
    private void initListeners() {
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                System.out.println("FOCUS");
                displayService.setFrame(editorService.getFrame());
            }
            @Override
            public void windowLostFocus(WindowEvent e) { }
        });

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

        fileService.addListener(new FileService.Adapter() {
            @Override
            public void fileChanged(File file) {
                setTitle(file.getName());
            }
        });
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        menuBar = new EditorMenuBar(editorService, fileService);
        setJMenuBar(menuBar);

        JPanel centerPanel = new JPanel(new java.awt.GridLayout(1, 2));
        circlePanel = new CircleEditPanel(editorService);
        centerPanel.add(circlePanel);
        rectPanel = new RectEditPanel(editorService);
        centerPanel.add(rectPanel);
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new java.awt.BorderLayout());

        statusLabel = new JLabel("Starting...");
        footerPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        JButton reconnectButton = new JButton("Reconnect");
        reconnectButton.setEnabled(false);
        footerPanel.add(reconnectButton, java.awt.BorderLayout.LINE_END);

        getContentPane().add(footerPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }

    private JLabel statusLabel;
}
