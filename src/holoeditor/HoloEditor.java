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

/**
 *
 * @author nehardt
 */
public class HoloEditor {

    static EditorJFrame editorFrame;
    static EditorService editorService;
    
    public static void main(String[] args) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting ">
        /* If system look and feel is not available, stay with the default look and feel.
         */
        try {
            String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if (systemLookAndFeel.equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(EditorJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            editorService = new EditorService();
            editorService.setFrame(new holoeditor.model.Frame(8, 8, 32));
            editorFrame = new EditorJFrame(editorService);
            editorFrame.setVisible(true);
        });
    }
}
