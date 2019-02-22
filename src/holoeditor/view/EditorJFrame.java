package holoeditor.view;

import holoeditor.model.PointTYR;
import holoeditor.service.*;

import java.awt.*;
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
    ColorChooser colorChooser;
    
    public EditorJFrame(
            EditorService editorService,
            DisplayService displayService,
            FileService fileService
    ) {
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

        setSize(800, 500);
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

        editorService.addListener(new EditorService.Adapter() {
            @Override
            public void colorChanged() {
                boolean nextColor = !brush.getColor();
                brush.setColor(nextColor);
                colorChooser.setColor(nextColor);
            }

            @Override
            public void frameChanged() {
                displayService.setFrame(editorService.getFrame());
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
        circlePanel = new CircleEditPanel(editorService, brush);
        centerPanel.add(circlePanel);
        rectPanel = new RectEditPanel(editorService, brush);
        centerPanel.add(rectPanel);
        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        JPanel footerPanel = newFooterPanel();
        getContentPane().add(footerPanel, java.awt.BorderLayout.PAGE_END);

        pack();
        weightSlider.grabFocus();
    }

    JPanel newFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.LINE_AXIS));

        statusLabel = new JLabel(displayService.isConnected()
                ? "Connected" : "Disconnected");
        statusLabel.setPreferredSize(new Dimension(80, 20));
        footerPanel.add(statusLabel);

        colorChooser = new ColorChooser(color -> {
            if (color != brush.getColor()) {
                editorService.changeColor();
            }
        });
        footerPanel.add(colorChooser);

        int K = 10; // Slider uses integers, so 1 slider tick = 0.1 brush weight.
        int MIN = 2;
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
        weightSlider.addChangeListener(
                e -> brush.setWeight(weightSlider.getValue() / (double)K));
        footerPanel.add(weightSlider);

        return footerPanel;
    }

    private JLabel statusLabel;
    private JSlider weightSlider;
}
