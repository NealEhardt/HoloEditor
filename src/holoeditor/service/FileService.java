/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.HoloEditor;
import holoeditor.model.Frame;
import java.awt.Component;
import java.awt.FileDialog;
import java.io.*;
import java.util.HashSet;
import java.util.function.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;

/**
 * Reads and writes .HOL files, presenting dialogs as needed.
 * A .HOL file consists of two serialized objects, gzipped together.
 * The first is an "intro string" with version info.
 * The second is a Frame object.
 * @author nehardt
 */
public class FileService {
    EditorService editorService;
    File file;
    Frame frame;
    final static String introString = "HOL0.0.1";
    HashSet<Listener> listeners = new HashSet<>();
    
    public FileService(EditorService editorService) {
        this.editorService = editorService;
    }
    
    public interface Listener {
        public void fileChanged(File file);
        public void frameChanged(Frame frame);
    }
    public static class Adapter implements Listener {
        @Override
        public void fileChanged(File file) { }
        @Override
        public void frameChanged(Frame frame) { }
    }
    
    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }
    public Frame getFrame() { return frame; }
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public static void openFile(Component parent) {
        FileDialog fd = makeFileDialog(parent, "Open", FileDialog.LOAD);
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length > 0) {
            File f = files[0];
            readFromFile(f, (newFrame, ex) -> {
                if (ex != null) {
                    String msg = ex.getMessage()+" Cannot open file "
                            +f.getAbsolutePath()+".";
                    JOptionPane.showMessageDialog(parent, msg,
                            "File read error "+f.getName(),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                HoloEditor.makeNewWindow(f, newFrame);
            });
        }
    }
    
    public void saveFile(Component parent) {
        if (file == null) {
            saveFileAs(parent);
            return;
        }
        writeToFile((ex) -> {
            if (ex != null) {
                String msg = ex.getMessage()+" Cannot write to file "
                        +file.getAbsolutePath()+". Try again?";
                int option = JOptionPane.showConfirmDialog(parent, msg,
                        "File write error "+file.getName(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    saveFile(parent);
                }
            }
        });
    }
    
    public void saveFileAs(Component parent) {
        FileDialog fd = makeFileDialog(parent, "Save As", FileDialog.SAVE);
        fd.setFile("Untitled.hol");
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length > 0) {
            file = files[0];
            if (!file.getName().endsWith(".hol")) {
                file = new File(file.getAbsolutePath()+".hol");
            }
            for (Listener l : listeners) {
                l.fileChanged(file);
            }
            saveFile(parent);
        }
    }
    
    private static FileDialog makeFileDialog(Component parent, String title, int mode) {
        java.awt.Frame parentFrame
                = (java.awt.Frame)javax.swing.SwingUtilities.getRoot(parent);
        FileDialog fd = new FileDialog(parentFrame, title, mode);
        fd.setFilenameFilter((dir, name) -> name.endsWith(".hol"));
        return fd;
    }
    
    public static void readFromFile(File file, BiConsumer<Frame, Exception> callback) {
        new Thread(() -> {
            try (
                FileInputStream fis = new FileInputStream(file);
                GZIPInputStream gis = new GZIPInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(gis);
            ) {
                Frame frame = readObjects(ois);
                java.awt.EventQueue.invokeLater(() -> callback.accept(frame, null));
            } catch (IOException ex) {
                java.awt.EventQueue.invokeLater(() -> callback.accept(null, ex));
            }
        }, "File reader").start();
    }
    
    public void writeToFile(Consumer<Exception> callback) {
        new Thread(() -> {
            try (
                FileOutputStream fos = new FileOutputStream(file);
                GZIPOutputStream gos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(gos);
            ) {
                writeObjects(oos);
                java.awt.EventQueue.invokeLater(() -> callback.accept(null));
            } catch (IOException ex) {
                java.awt.EventQueue.invokeLater(() -> callback.accept(ex));
            }
        }).start();
    }
    
    private void writeObjects(ObjectOutputStream oos) throws IOException {
        oos.writeObject(introString);
        oos.writeObject(editorService.getFrame());
    }
    
    private static Frame readObjects(ObjectInputStream ois) throws IOException {
        try {
            Object first = ois.readObject();
            if (!introString.equals(first)) {
                throw new IOException("Unrecognized intro string.");
            }
            Object second = ois.readObject();
            if (!(second instanceof Frame)) {
                throw new IOException("Second object is not a Frame.");
            }
            return (Frame)second;
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }
}
