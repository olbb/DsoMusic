package com.dirror.music.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dirror.music.App
import com.dirror.music.util.Config
import kotlin.system.exitProcess

class BootUpReceiver(): BroadcastReceiver() {

    companion object {
        private const val TAG = "BootUpReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive:$intent")
        intent?.let {
            when(intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> {
                    val autoStart = App.mmkv.decodeBool(Config.AUTO_START_ON_BOOT_UP, false)
                    Log.d(TAG, "BootUpReceiver autoStart $autoStart")
                    if (!autoStart) {
                        exitProcess(0)
                    }
                }
            }
        }

    }
}