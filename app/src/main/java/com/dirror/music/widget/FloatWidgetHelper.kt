package com.dirror.music.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dirror.lyricviewx.LyricViewX
import com.dirror.music.MyApp
import com.dirror.music.R
import com.dirror.music.music.standard.data.StandardSongData
import com.dirror.music.service.MusicService
import com.dirror.music.ui.main.MainActivity
import com.dirror.music.ui.player.PlayerActivity
import com.dirror.music.util.Api
import com.dirror.music.util.Config
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FloatWidgetHelper {

    companion object {
        const val TAG = "FloatWidgetHelper"

        @SuppressLint("StaticFieldLeak")
        private var title: TextView? = null
        @SuppressLint("StaticFieldLeak")
        private var singer: TextView? = null
        @SuppressLint("StaticFieldLeak")
        private var lyricView: LyricViewX? = null


        val handler = Handler(Looper.getMainLooper())

        val updateRunnable = Runnable {
            if (MyApp.musicController.value?.isPlaying()?.value == true) {
                MyApp.musicController.value?.getProgress()?.let { updateProgress(it) }
            }
        }


        fun initWidget() {
            if (!MyApp.mmkv.decodeBool(Config.FLOAT_PLAY_INFO)) {
                EasyFloat.dismiss()
                return
            }
            Log.i(TAG, "initWidget")
            EasyFloat.with(MyApp.context).setLayout(R.layout.float_widget)
                .setShowPattern(ShowPattern.BACKGROUND)
                .setDragEnable(true)
                .setSidePattern(SidePattern.RESULT_SIDE)
                .registerCallback {
                    createResult { isCreated, msg, view ->
                        Log.d(TAG, "createResult: $isCreated $msg")
                        view?.let {
                            initView(it)
                        }
                    }
                    show {
                        Log.d(TAG, "show")
                    }
                    hide {
                        Log.d(TAG, "hide")
                        handler.removeCallbacksAndMessages(null)
                    }
                    dismiss {
                        handler.removeCallbacksAndMessages(null)
                        title = null
                        singer = null
                        lyricView = null
                    }
                    touchEvent { view, motionEvent ->  }
                    drag { view, motionEvent ->  }
                    dragEnd {  }
                }
                .show()
        }

        fun setPlayInfo(info: StandardSongData) {
            title?.text = info.name
            info.artists?.let {singer?.text = getSingerList(it)}
            info.id?.let {
                GlobalScope.launch { Api.getLyricUrl(it)?.lrc?.let {
                    lyricView?.loadLyric(it.lyric)
                    handler.postDelayed(updateRunnable, 500)
                } }
            }

        }

        private fun updateProgress(pr : Long) {
            lyricView?.updateTime(pr)
            handler.postDelayed(updateRunnable, 500)
        }

        private fun initView(v: View) {
            v.setOnClickListener{
                val intentMain = Intent(MyApp.context, MainActivity::class.java)
                val intentPlayer = Intent(MyApp.context, PlayerActivity::class.java)
                intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                MyApp.context.startActivities(arrayOf(intentMain, intentPlayer))
            }
            title = v.findViewById(R.id.title)
            singer = v.findViewById(R.id.singer)
            lyricView = v.findViewById(R.id.lyricView)
        }


        private fun getSingerList(singers: List<StandardSongData.StandardArtistData>):String {
            val buffer = StringBuffer()
            for (singer in singers) {
                if (buffer.isNotEmpty()) {
                    buffer.append(" ")
                }
                buffer.append(singer.name)
            }
            return buffer.toString()
        }
    }


}