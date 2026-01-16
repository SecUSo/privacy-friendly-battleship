/*
    Copyright 2025 Christian Adams

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
    along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.secuso.privacyfriendlybattleship.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.createBitmap
import org.secuso.privacyfriendlybattleship.R
import org.secuso.privacyfriendlybattleship.util.LogTag

/**
 * This class provides the resources for the battleships game.
 * Goal is to load resources once and reuse them.
 *
 * @author Christian Adams
 */
object GameResources {
    private val TAG = LogTag.create(this::class.java)

    val DUMMY_BITMAP = createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    private var shipsBitmaps: Array<Array<Array<Bitmap?>?>> = arrayOf(
        arrayOfNulls(4),
        arrayOfNulls(4),
        arrayOfNulls(4),
        arrayOfNulls(4))

    fun loadGameResources(context: Context) {
        val shipsBitmapsNorth = shipsBitmaps[Direction.NORTH.ordinal]
        shipsBitmapsNorth[0] = loadShipBitmaps(context, R.drawable.ship_2, 2)
        shipsBitmapsNorth[1] = loadShipBitmaps(context, R.drawable.ship_3, 3)
        shipsBitmapsNorth[2] = loadShipBitmaps(context, R.drawable.ship_4, 4)
        shipsBitmapsNorth[3] = loadShipBitmaps(context, R.drawable.ship_5, 5)

        val shipsBitmapsEast = shipsBitmaps[Direction.EAST.ordinal]
        val shipsBitmapsSouth = shipsBitmaps[Direction.SOUTH.ordinal]
        val shipsBitmapsWest = shipsBitmaps[Direction.WEST.ordinal]
        for (i in 0 .. 3) {
            shipsBitmapsEast[i] = rotateShipBitmaps(shipsBitmapsNorth[i]!!, 90.0f)
            shipsBitmapsSouth[i] = rotateShipBitmaps(shipsBitmapsNorth[i]!!, 180.0f)
            shipsBitmapsWest[i] = rotateShipBitmaps(shipsBitmapsNorth[i]!!, 270.0f)
        }
    }

    private fun loadShipBitmaps(context: Context, resourceId: Int, parts: Int): Array<Bitmap?> {
        val ship = BitmapFactory.decodeResource(context.resources, resourceId)
        val w = ship.width
        val h = ship.height
        val partHeight: Int = h / parts
        // Images automatically get scaled at loading. So these warnings would appear by default. Avoid that.
        /*
        if (partHeight * parts != h) {
            Log.w(TAG, "The length of the ship ($h px) is not divisible by $parts without a remainder.")
        }
        if (partHeight != w) {
            Log.w(TAG, "The part-height of the ship ($partHeight px) is different to ship width ($w) -> Parts are no squares.")
        }
        */
        val result = arrayOfNulls<Bitmap>(parts)
        for (i in 0 until parts) {
            result[i] = Bitmap.createBitmap(ship, 0, i * partHeight, w, partHeight)
        }
        return result
    }

    private fun rotateShipBitmaps(shipBitmaps: Array<Bitmap?>, degrees: Float): Array<Bitmap?> {
        val matrix = Matrix().apply { postRotate(degrees) }
        val result: Array<Bitmap?> = arrayOfNulls(shipBitmaps.size)
        var index = -1
        for (bitmap in shipBitmaps) {
            ++index
            if (null != bitmap) {
                result[index] = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        }
        return result
    }

    fun getShipBitmap(direction: Direction, shipSize: Int, part: Int): Bitmap {
        val shipIndex = shipSize - 2
        return try {
            shipsBitmaps[direction.ordinal][shipIndex]!![part]!!
        } catch (e: Exception) {
            if (null == shipsBitmaps[0][0]) {
                Log.e(TAG, "getShipBitmap() fails: Ship bitmaps not loaded yet. Error: $e")
            } else {
                Log.w(TAG, "Invalid arguments at getShipBitmap(): Direction $direction, ship size $shipSize, part $part. Error: $e")
            }

            // Fallback to ensure to not return null
            DUMMY_BITMAP
        }
    }
}
