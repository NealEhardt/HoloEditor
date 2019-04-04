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
public class EditorJFrame extends JFrame implements EditorMenuBar.Delegate {
    EditorService editorService;
    DisplayService displayService;
    FileService fileService;
    Brush brush;

    private ColorChooser colorChooser;
    private JLabel statusLabel;
    private JSlider weightSlider;
    
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

        EditorMenuBar menuBar = new EditorMenuBar(this, fileService);
        setJMenuBar(menuBar);

        JPanel centerPanel = new JPanel(new java.awt.GridLayout(1, 2));
        CircleEditPanel circlePanel = new CircleEditPanel(editorService, brush);
        centerPanel.add(circlePanel);
        RectEditPanel rectPanel = new RectEditPanel(editorService, brush);
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
                changeColor();
            }
        });
        footerPanel.add(colorChooser);
        footerPanel.add(newShapePanel());
        footerPanel.add(newSymmetryPanel());
        footerPanel.add(newWeightSlider());

        return footerPanel;
    }

    JPanel newSymmetryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        leftPanel.add(new JLabel("Symmetries"));

        JComboBox<String> box = new JComboBox<>(new String[]{
                "1", "2", "3", "4", "5", "6", "7", "8"});
        box.addActionListener(e -> {
            brush.setSymmetries(box.getSelectedIndex() + 1);
        });
        leftPanel.add(box);
        panel.add(leftPanel);

        JCheckBox checkBox = new JCheckBox("Mirror Vertical");
        checkBox.addActionListener(e -> {
            brush.setMirror(checkBox.isSelected());
        });
        panel.add(checkBox);

        return panel;
    }

    JPanel newShapePanel() {
        JPanel shapePanel = new JPanel();
        shapePanel.setLayout(new BoxLayout(shapePanel, BoxLayout.PAGE_AXIS));

        shapePanel.add(new JLabel("Brush shape"));

        JComboBox<String> shapeBox = new JComboBox<>(new String[]{"Circle", "Sphere"});
        shapeBox.addActionListener(e -> {
            brush.setShape(shapeBox.getSelectedIndex() == 0
                    ? Brush.Shape.Circle : Brush.Shape.Sphere);
        });
        shapePanel.add(shapeBox);

        return shapePanel;
    }

    JSlider newWeightSlider() {
        int K = 5; // Slider uses integers, so 1 slider tick = 0.2 brush weight.
        int MIN = 1;
        weightSlider = new JSlider(MIN, 5*K, K);
        weightSlider.setMajorTickSpacing(1);
        Hashtable<Integer, JComponent> table = new Hashtable<>();
        for (int i = K; i <= weightSlider.getMaximum(); i += K) {
            table.put(i, new JLabel(Integer.toString(i/K)));
        }
        weightSlider.setLabelTable(table);
        weightSlider.setPaintLabels(true);
        weightSlider.setPaintTicks(true);
        weightSlider.addChangeListener(e -> {
            brush.setWeight(weightSlider.getValue() / (double)K);
            repaint();
        });
        return weightSlider;
    }

    @Override
    public void changeColor() {
        boolean nextColor = !brush.getColor();
        brush.setColor(nextColor);
        colorChooser.setColor(nextColor);
    }

    @Override
    public void changeWeight(int amount) {
        weightSlider.setValue(weightSlider.getValue() + amount);
    }
}
