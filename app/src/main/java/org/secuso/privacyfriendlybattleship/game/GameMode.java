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
