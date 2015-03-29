/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor;

import holoeditor.service.*;
import holoeditor.view.EditorJFrame;
import javax.swing.UIManager;
import java.util.logging.*;
import javax.swing.JFrame;

/**
 *
 * @author nehardt
 */
public class HoloEditor {

    static SerialService serialService;
    
    public static void main(String[] args) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting ">
        /* If system look and feel is not available, stay with the default look and feel.
         */
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
        //</editor-fold>
        
        java.awt.EventQueue.invokeLater(() -> {
            serialService = new SerialService();
            makeNewWindow();
            serialService.tryNextPort();
        });
    }
    
    public static void makeNewWindow() {
        EditorService editorService = new EditorService(serialService);
        FileService fileService = new FileService(editorService);
        editorService.setFrame(new holoeditor.model.Frame(32, 8, 8));
        EditorJFrame editorFrame = new EditorJFrame(editorService, serialService, fileService);
        editorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editorFrame.setVisible(true);
    }
}
