package holoeditor;

import holoeditor.model.Frame;
import holoeditor.service.*;
import holoeditor.view.EditorJFrame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import javax.swing.UIManager;
import java.util.logging.*;
import javax.swing.JFrame;

/**
 * Root of editor. Manages multiple windows.
 * @author Neal Ehardt
 */
public class HoloEditor {
    static DisplayService displayService;
    
    public static void main(String[] args) {
        trySetSystemLookAndFeel();

        java.awt.EventQueue.invokeLater(() -> {
            displayService = new DisplayService();
            makeNewWindow();
            displayService.start();
        });
    }

    private static void trySetSystemLookAndFeel() {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                String className = info.getClassName();
                if (systemLookAndFeel.equals(className)) {
                    javax.swing.UIManager.setLookAndFeel(className);
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EditorJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static int windowCount = 0;
    
    public static void makeNewWindow() {
        makeNewWindow(null, null);
    }
    
    public static void makeNewWindow(File file, Frame frame) {
        EditorService editorService = new EditorService(displayService);
        if (frame == null) {
            frame = new Frame();
        }
        editorService.setFrame(frame);
        
        FileService fileService = new FileService(editorService);
        fileService.setFile(file);
        
        EditorJFrame editorFrame = new EditorJFrame(editorService,
                displayService, fileService);
        String title = "Untitled.hol";
        if (file != null) {
            title = file.getName();
        }
        editorFrame.setTitle(title);
        
        editorFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                windowCount++;
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                windowCount--;
                if (windowCount == 0) {
                    System.exit(0);
                }
            }
        });

        editorFrame.setLocationByPlatform(true); // stagger new windows
        editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editorFrame.setVisible(true);
    }
}
