/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * GameMode.java is part of Privacy Friendly Battleship.
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

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.secuso.privacyfriendlybattleship.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexander Müller on 16.12.2016. Edited by Ali Kalsen on 16.01.2017.
 */

public enum GameMode {
    VS_PLAYER(R.string.mode_two_player, R.drawable.ic_people_black_24px),
    VS_AI_EASY(R.string.mode_vs_cpu_easy, R.drawable.ic_cpu_easy),
    VS_AI_HARD(R.string.mode_vs_cpu_hard, R.drawable.ic_cpu_hard),
    CUSTOM(R.string.mode_custom, R.drawable.ic_people_black_24px);

    private final int resIDString;
    private final int resIDImage;
    private static List<GameMode> validTypes = new LinkedList<>();


    static{
        validTypes.add(VS_PLAYER);
        validTypes.add(VS_AI_EASY);
        validTypes.add(VS_AI_HARD);
    }


    GameMode(@StringRes int resIDString, @DrawableRes int resIDImage){
        this.resIDString = resIDString;
        this.resIDImage = resIDImage;
    }

    public int getStringResID() {
        return resIDString;
    }

    public int getImageResID(){
        return resIDImage;
    }

    public static List<GameMode> getValidTypes(){
        return validTypes;
    }
}
