/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.*;

/**
 *
 * @author Neal Ehardt
 */
public class DisplayService {
    final SerialService serialService;
    
    javax.swing.Timer sliceTimer;
    Frame frame;
    int sliceTheta;
    
    public DisplayService(SerialService serialService) {
        this.serialService = serialService;
        
        serialService.addListener(new SerialService.Adapter() {
            @Override
            public void connectedToPort(String portName) {
                sliceTimer = new javax.swing.Timer(1, (ae) ->  {
                    if (sliceTheta >= frame.circumference) { sliceTheta = 0; }
                    serialService.writePacket(frame.getPacket(sliceTheta));
                    sliceTheta++;
                });
                sliceTimer.setRepeats(true);
                sliceTimer.start();
            }

            @Override
            public void disconnectedFromPort(String portName) {
                sliceTimer.stop();
            }

            @Override
            public void recievedPacket(byte[] packet) {
                
            }
        });
    }
    
    public void setFrame(Frame frame) {
        this.frame = frame;
    }
}
