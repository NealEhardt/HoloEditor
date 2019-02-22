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
    public interface Delegate {
        void changeColor();
        void changeWeight(int amount);
    }

    private FileService fileService;
    private Delegate delegate;
    
    public EditorMenuBar(Delegate delegate, FileService fileService) {
        this.fileService = fileService;
        this.delegate = delegate;

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

        // Ctrl on Win and Nix; Cmd on Mac
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        JMenuItem colorItem = new JMenuItem("Change color");
        colorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        colorItem.addActionListener((e) -> delegate.changeColor());
        menu.add(colorItem);

        JMenuItem decreaseItem = new JMenuItem("Decrease brush weight");
        decreaseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0));
        decreaseItem.addActionListener((e) -> delegate.changeWeight(-1));
        menu.add(decreaseItem);

        JMenuItem decrease5Item = new JMenuItem("Decrease brush weight x5");
        decrease5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, menuMask));
        decrease5Item.addActionListener((e) -> delegate.changeWeight(-5));
        menu.add(decrease5Item);

        JMenuItem increaseItem = new JMenuItem("Increase brush weight");
        increaseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0));
        increaseItem.addActionListener((e) -> delegate.changeWeight(1));
        menu.add(increaseItem);

        JMenuItem increase5Item = new JMenuItem("Increase brush weight x5");
        increase5Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, menuMask));
        increase5Item.addActionListener((e) -> delegate.changeWeight(5));
        menu.add(increase5Item);

        return menu;
    }
}
