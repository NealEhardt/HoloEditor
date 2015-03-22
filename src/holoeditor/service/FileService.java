/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.Frame;
import java.awt.FileDialog;
import java.io.*;
import java.util.function.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
    final String introString = "HOL0.0.1";
    
    public FileService(EditorService editorService) {
        this.editorService = editorService;
    }
    
    public void openFile(java.awt.Frame parent) {
        FileDialog fd = new FileDialog(parent, "Open", FileDialog.LOAD);
        fd.setFilenameFilter((dir, name) -> name.endsWith(".hol"));
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length > 0) {
            file = files[0];
            System.out.println("You chose " + file);
            readFromFile((frame, ex) -> {
                if (ex != null) {
                    System.err.println("oh no "+ex);
                    return;
                }
                editorService.setFrame(frame);
            });
        }
    }
    
    public void saveFile(java.awt.Frame parent) {
        if (file == null || !file.canWrite()) {
            saveFileAs(parent);
            return;
        }
        writeToFile((ex) -> {
            if (ex != null) {
                System.err.println("oh no "+ex);
            } else {
                System.out.println("File saved.");
            }
        });
    }
    
    public void saveFileAs(java.awt.Frame parent) {
        FileDialog fd = new FileDialog(parent, "Save As", FileDialog.SAVE);
        fd.setFile("Untitled.hol");
        fd.setFilenameFilter((dir, name) -> name.endsWith(".hol"));
        fd.setVisible(true);
        File[] files = fd.getFiles();
        if (files.length > 0) {
            file = files[0];
            System.out.println("You chose " + file);
            saveFile(parent);
        }
    }
    
    private void readFromFile(BiConsumer<Frame, Exception> callback) {
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
        }).start();
    }
    
    private void writeToFile(Consumer<Exception> callback) {
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
    
    private Frame readObjects(ObjectInputStream ois) throws IOException {
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
