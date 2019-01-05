/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.view;

import holoeditor.model.PointTYR;
import holoeditor.service.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.Hashtable;
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
    Brush brush;

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
        brush = new Brush(new Brush.Delegate() {
            @Override
            public void setVoxel(PointTYR point, boolean color) {
                editorService.setVoxel(point, color);
            }

            @Override
            public void commitChanges() {
                editorService.commitChanges();
            }
        });
        
        initComponents();
        initListeners();

        setSize(700, 400);
    }
    
    private void initListeners() {
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                displayService.setFrame(editorService.getFrame());
            }
            @Override
            public void windowLostFocus(WindowEvent e) { }
        });

        displayService.addListener(new DisplayService.Listener() {
            @Override
            public void connected() {
                statusLabel.setText("Connected");
                if (EditorJFrame.this.isFocused()) {
                    displayService.setFrame(editorService.getFrame());
                }
            }

            @Override
            public void disconnected() {
                statusLabel.setText("Disconnected");
            }
        });

        editorService.addListener(new EditorService.Listener() {
            @Override
            public void frameChanged() {
                displayService.setFrame(editorService.getFrame());
            }

            @Override
            public void thetaChanged(int theta) { }

            @Override
            public void yChanged(int y) { }
        });

        fileService.addListener(new FileService.Adapter() {
            @Override
            public void fileChanged(File file) {
                setTitle(file.getName());
            }
        });

        weightSlider.addChangeListener(e -> brush.setWeight(weightSlider.getValue() / 10d));
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        menuBar = new EditorMenuBar(editorService, fileService);
        setJMenuBar(menuBar);

        JPanel centerPanel = new JPanel(new java.awt.GridLayout(1, 2));
        circlePanel = new CircleEditPanel(editorService, brush);
        centerPanel.add(circlePanel);
        rectPanel = new RectEditPanel(editorService, brush);
        centerPanel.add(rectPanel);
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new java.awt.BorderLayout());

        statusLabel = new JLabel(displayService.isConnected()
                                    ? "Connected" : "Disconnected");
        footerPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        int K = 10; // Slider uses integers, so 1 slider tick = 0.1 brush weight.
        int MIN = 8; // When weight < sqrt(6)/4 ≈ 0.61, the Brush has issues.
        weightSlider = new JSlider(MIN, 5*K, 2*K);
        weightSlider.setMajorTickSpacing(2);
        weightSlider.setMinorTickSpacing(1);
        Hashtable<Integer, JComponent> table = new Hashtable<>();
        for (int i = K; i <= weightSlider.getMaximum(); i += K) {
            table.put(i, new JLabel(Integer.toString(i/K)));
        }
        weightSlider.setLabelTable(table);
        weightSlider.setPaintLabels(true);
        weightSlider.setPaintTicks(true);
        footerPanel.add(weightSlider, java.awt.BorderLayout.LINE_END);

        getContentPane().add(footerPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }

    private JLabel statusLabel;
    private JSlider weightSlider;
}
