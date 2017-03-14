/*
    Copyright 2017 Alexander Müller, Ali Kalsen

    This file is part of Privacy Friendly Battleships.

    Privacy Friendly Battleships is free software: you can redistribute
    it and/or modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Privacy Friendly Battleships is distributed in the hope that it will
    be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see http://www.gnu.org/licenses/.
 */

package org.secuso.privacyfriendlybattleship.game;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.secuso.privacyfriendlybattleship.R;

import java.util.LinkedList;
import java.util.List;

/**
 * This file represents the mode for a battleships game. It is used to
 * define whether the game is played in the two player mode or against the
 * AI in one of two difficulty levels.
 *
 * @author Alexander Müller, Ali Kalsen
 */

public enum GameMode {
    VS_PLAYER(R.string.mode_two_player, R.drawable.ic_people_black_24px),
    VS_AI_EASY(R.string.mode_vs_cpu_easy, R.drawable.ic_person_black_24px),
    VS_AI_HARD(R.string.mode_vs_cpu_hard, R.drawable.ic_person_black_24px),
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
