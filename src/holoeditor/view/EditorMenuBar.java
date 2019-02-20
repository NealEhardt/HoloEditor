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
    
    public EditorMenuBar(
            EditorService editorService,
            FileService fileService) {
        this.editorService = editorService;
        this.fileService = fileService;

        this.add(newFileMenu());
        this.add(newEditMenu());
    }
    
    private JMenu newFileMenu() {
        JMenu fileMenu = new JMenu("File");

        // Ctrl on Win and Nix; Cmd on Mac
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuMask));
        newItem.addActionListener((e) -> HoloEditor.makeNewWindow());
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuMask));
        openItem.addActionListener((e) -> FileService.openFile(this));
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuMask));
        saveItem.addActionListener((e) -> fileService.saveFile(this));
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                menuMask | InputEvent.SHIFT_DOWN_MASK));
        saveAsItem.addActionListener((e) -> fileService.saveFileAs(this));
        fileMenu.add(saveAsItem);

        return fileMenu;
    }
    
    private JMenu newEditMenu() {
        JMenu menu = new JMenu("Edit");

        JMenuItem colorItem = new JMenuItem("Change Color");
        colorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        colorItem.addActionListener((e) -> editorService.changeColor());
        menu.add(colorItem);

        return menu;
    }
}
