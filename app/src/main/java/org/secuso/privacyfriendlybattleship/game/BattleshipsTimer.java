package org.secuso.privacyfriendlybattleship.game;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Ali Kalsen on 27.01.2017.
 */

public class BattleshipsTimer {

    private Timer internalTimer;
    private AtomicBoolean timerRunning = new AtomicBoolean(false);
    private int time = 0;
    private final int WAIT_TIME = 1000;

    private void init() {
        internalTimer = new Timer();
        internalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if ( timerRunning.get()) {
                    time++;
                }
            }
        },0 ,WAIT_TIME);
    }

    public void stop(){
        timerRunning.set(false);
    }

    public void start(){
        timerRunning.set(true);
        if(internalTimer == null){
            init();
        }
    }

    public int getTime(){
        return time;
    }
}
