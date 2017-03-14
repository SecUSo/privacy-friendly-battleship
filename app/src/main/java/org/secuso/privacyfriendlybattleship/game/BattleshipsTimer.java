/*
    Copyright 2017 Alexander Müller, Ali Kalsen

    This file is part of Privacy Friendly Battleships.

    Privacy Friendly Battleships is free software: you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or any later version.

    Privacy Friendly Battleships is distributed in the hope that it will
    be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see http://www.gnu.org/licenses/.
 */

package org.secuso.privacyfriendlybattleship.game;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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
