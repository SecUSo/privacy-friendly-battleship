/**
 * Copyright (c) 2017, Alexander Müller, Ali Kalsen and affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * AboutActivity.java is part of Privacy Friendly Battleship.
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
 * along with Privacy Friendly Battleship. If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlybattleship.ui

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.secuso.privacyfriendlybattleship.BuildConfig
import org.secuso.privacyfriendlybattleship.R

/**
 * Created by yonjuni on 15.06.16.
 */
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar: Toolbar = findViewById(R.id.toolbar_about)
        setSupportActionBar(toolbar)
        val supportActionBarRef = supportActionBar
        if (supportActionBarRef != null) {
            supportActionBarRef.setDisplayHomeAsUpEnabled(true)
            supportActionBarRef.setDisplayShowHomeEnabled(true)
        }

        val mainContent = findViewById<View>(R.id.main_content)
        if (mainContent != null) {
            mainContent.alpha = 0f
            mainContent.animate().alpha(1f)
                .setDuration(BaseActivity.MAIN_CONTENT_FADEIN_DURATION.toLong())
        }

        findViewById<TextView>(R.id.secusoWebsite).movementMethod = LinkMovementMethod.getInstance()
        findViewById<TextView>(R.id.githubURL).movementMethod = LinkMovementMethod.getInstance()
        findViewById<TextView>(R.id.textFieldVersionName).text = BuildConfig.VERSION_NAME

        overridePendingTransition(0, 0)
    }
}
