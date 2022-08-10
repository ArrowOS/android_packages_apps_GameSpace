/*
 * Copyright (C) 2021 Chaldeaprjkt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.chaldeaprjkt.gamespace.widget

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.utils.dp
import io.chaldeaprjkt.gamespace.utils.isPortrait
import kotlin.math.max
import kotlin.math.min

class PanelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var defaultY: Float? = null
    var relativeY = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.panel_view, this, true)
        isClickable = true
        isFocusable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyRelativeLocation()
        getMemory()
        batteryTemperature()
        getBatteryStat()
    }

    private fun getMemory() {
        val mAm = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo:TextView = findViewById(R.id.memoryInfo)
        val memInfo = MemoryInfo()
        mAm.getMemoryInfo(memInfo)
        val available = (memInfo.availMem / 1048576L).toInt()
        val max = (memInfo.totalMem / 1048576L).toInt()
        memoryInfo.text = "$available/$max MB"
    }

    private fun batteryTemperature() {
        val intent: Intent =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toFloat() / 10
        val degree = "\u2103"
        val batteryTemp:TextView = findViewById(R.id.batteryTemp)
        batteryTemp.text = "$temp$degree"
    }

    private fun getBatteryStat() {
        val sampleText:TextView = findViewById(R.id.batteryPct)
        val mBatInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level * 100 / scale
                sampleText.text = "$batteryPct%"
            }
        }

        context.registerReceiver(mBatInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun applyRelativeLocation() {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams.height = LayoutParams.WRAP_CONTENT

        doOnLayout {
            if (defaultY == null)
                defaultY = y

            y = if (wm.isPortrait()) {
                val safeArea = rootWindowInsets.getInsets(WindowInsets.Type.systemBars())
                val minY = safeArea.top + 16.dp
                val maxY = safeArea.top + (parent as View).height - safeArea.bottom - height - 16.dp
                min(max(relativeY, minY), maxY).toFloat()
            } else {
                defaultY ?: 16f
            }

        }
    }

}
