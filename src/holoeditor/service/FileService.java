/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import java.io.File;

/**
 *
 * @author nehardt
 */
public class FileService {
    public void writeToFile(File file, Callback callback) {
        new Thread(
                () -> callback.fileWriteComplete("Not implemented")
        ).start();
    }

    public interface Callback {
        public void fileWriteComplete(String errorMessage);
    }
}
