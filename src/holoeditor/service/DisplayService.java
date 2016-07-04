/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoeditor.service;

import holoeditor.model.*;
import java.util.concurrent.TimeUnit;

/**
 * Takes a Frame and rhythmically transmits its slices to SerialService
 * 
 * @author Neal Ehardt
 */
public class DisplayService {
    final SerialService serialService;
    
    Thread thread;
    Frame frame;
    volatile int sliceTheta;
    volatile long period = 2000000000L;
    volatile long lastTick = System.nanoTime();
    
    public DisplayService(SerialService serialService) {
        this.serialService = serialService;
        
        serialService.addListener(new SerialService.Adapter() {
            @Override
            public void connectedToPort(String portName) {
                thread = new Thread(() ->  {
                    while(thread == Thread.currentThread()) {
                        serialService.writePacket(frame.getPacket(sliceTheta % frame.circumference));
                        sliceTheta++;
                        
                        long nextSliceTime = lastTick + period * sliceTheta / frame.circumference;
                        long sleepTime = nextSliceTime - System.nanoTime();
                        if (sleepTime > 0) {
                            try {
                                //System.out.println("sleep " + sleepTime);
                                TimeUnit.NANOSECONDS.sleep(sleepTime);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void disconnectedFromPort(String portName) {
                thread = null;
            }

            @Override
            public void recievedPacket(String packet) {
                if ("tick".equals(packet)) {
                    long t = System.nanoTime();
                    long newPeriod = t - lastTick;
                    if (newPeriod > 10000000) {
                        period = t - lastTick;
                        lastTick = t;
                        sliceTheta = 0;
                        System.out.println("period = " + period);
                    }
                }
            }
        });
    }
    
    public void setFrame(Frame frame) {
        this.frame = frame;
    }
}
