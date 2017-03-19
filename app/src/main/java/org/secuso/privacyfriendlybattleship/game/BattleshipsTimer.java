/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * BattleshipsTimer.java is part of Privacy Friendly Battleship.
 *
 * Privacy Friendly Battleship is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Friendly Battleship is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Friendly Battleship. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlybattleship.game;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class creates a timer, which only counts full seconds, if it is running. Created on 27.01.2017.
 *
 * @author Ali Kalsen
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
