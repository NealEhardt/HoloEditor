package holoeditor.view;

import holoeditor.HoloEditor;
import holoeditor.service.*;
import java.awt.event.InputEvent;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.*;

/**
 *
 * @author nehardt
 */
public class EditorMenuBar extends JMenuBar {
    EditorService editorService;
    FileService fileService;
    
    JMenu fileMenu;
    JMenuItem newMenuItem;
    JMenuItem openMenuItem;
    JMenuItem saveMenuItem;
    JMenuItem saveAsMenuItem;
    
    JMenu editMenu;
    
    public EditorMenuBar(
            EditorService editorService,
            FileService fileService) {
        this.editorService = editorService;
        this.fileService = fileService;
        
        initFileMenu();
        this.add(fileMenu);
        
        initEditMenu();
        this.add(editMenu);
    }
    
    private void initFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.add(newMenuItem = new JMenuItem("New"));
        fileMenu.add(openMenuItem = new JMenuItem("Open..."));
        fileMenu.add(saveMenuItem = new JMenuItem("Save"));
        fileMenu.add(saveAsMenuItem = new JMenuItem("Save As..."));
        
        int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcutKeyMask));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuShortcutKeyMask));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyMask));
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                menuShortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
        
        newMenuItem.addActionListener((e) -> HoloEditor.makeNewWindow());
        openMenuItem.addActionListener((e) -> FileService.openFile(this));
        saveMenuItem.addActionListener((e) -> fileService.saveFile(this));
        saveAsMenuItem.addActionListener((e) -> fileService.saveFileAs(this));
    }
    
    private void initEditMenu() {
        editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("TODO"));
    }
}
