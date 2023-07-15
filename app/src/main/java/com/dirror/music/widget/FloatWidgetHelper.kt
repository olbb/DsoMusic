package com.dirror.music.widget

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.dirror.lyricviewx.LyricViewX
import com.dirror.music.App
import com.dirror.music.R
import com.dirror.music.manager.User
import com.dirror.music.music.standard.data.SOURCE_NETEASE
import com.dirror.music.music.standard.data.StandardSongData
import com.dirror.music.ui.main.MainActivity
import com.dirror.music.ui.player.PlayerActivity
import com.dirror.music.util.Api
import com.dirror.music.util.Config
import com.dirror.music.util.toast
import com.google.gson.Gson
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FloatWidgetHelper : LifecycleOwner {

    companion object {
        const val KEY_FLOAT_WIDGET_LOC_STR = "KEY_FLOAT_WIDGET_LOC_STR"
        const val TAG = "FloatWidgetHelper"
        const val EMPTY_LYRIC = "[00:00.000]......"
        @SuppressLint("StaticFieldLeak")
        val Ins = FloatWidgetHelper()
    }

    private var title: TextView? = null
    private var icPlay: ImageView? = null
    private var icLike: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var lyricView: LyricViewX? = null
    private val lifecycleRegistry = LifecycleRegistry(this)


    fun initWidget() {
        if (!App.mmkv.decodeBool(Config.FLOAT_PLAY_INFO)) {
            EasyFloat.dismiss()
            return
        }
        val locStr = App.mmkv.decodeString(KEY_FLOAT_WIDGET_LOC_STR, "")
        var loc = IntArray(2)
        if (locStr?.isNotEmpty() == true) {
            loc = Gson().fromJson(locStr, IntArray::class.java)
        }
        Log.i(TAG, "initWidget")
        EasyFloat.with(App.context).setLayout(R.layout.float_widget)
            .setShowPattern(ShowPattern.BACKGROUND)
            .setDragEnable(true)
            .setSidePattern(SidePattern.RESULT_SIDE)
            .setLocation(loc[0], loc[1])
            .registerCallback {
                createResult { isCreated, msg, view ->
                    Log.d(TAG, "createResult: $isCreated $msg")
                    view?.let {
                        initView(it)
                    }
                }
                show {
                    Log.d(TAG, "show")
                    setLifecycleState(Lifecycle.State.RESUMED)
                }
                hide {
                    Log.d(TAG, "hide")
                    setLifecycleState(Lifecycle.State.CREATED)
                }
                dismiss {
                    setLifecycleState(Lifecycle.State.DESTROYED)
                    title = null
                    lyricView = null
                    icLike = null
                    icPlay = null
                }
                touchEvent { view, motionEvent ->  }
                drag { view, motionEvent ->  }
                dragEnd {
                    val loc = IntArray(2)
                    it.getLocationOnScreen(loc)
                    App.mmkv.encode(KEY_FLOAT_WIDGET_LOC_STR, Gson().toJson(loc))
                }
            }
            .show()
        setLifecycleState(Lifecycle.State.CREATED)
    }

    private fun initObserver() {
        Log.i(TAG, "initObserver, lifecycleRegistry.currentState: ${lifecycleRegistry.currentState}")
        App.musicController.value?.let { controller ->
            controller.isPlaying().observe(this) {
                updatePlayStatus(it)
            }
            controller.getPlayingSongData().observe(this) {
                if (it != null) {
                    setPlayInfo(it)
                }
            }
            controller.getLyricEntryList().observe(this) {
                Log.d(TAG, "setLyricEntryList:$it")
                if (it.size > 0) {
                    lyricView?.setLyricEntryList(it)
                } else {
                    lyricView?.loadLyric(EMPTY_LYRIC)
                }
            }
            controller.getProgressLiveData().observe(this) {
                updateProgress(it)
            }
            controller.getDurationLiveData().observe(this) {
                Log.d(TAG, "duration update: $it")
                progressBar?.max = it.toInt()
            }
            User.userLikeData.observe(this) {
                if (it != null) {
                    val songId = controller.getPlayingSongData().value?.id?.toLong() ?: -1
                    val liked = it.contains(songId)
                    icLike?.setImageResource(if (liked) R.drawable.ic_player_heart else R.drawable.ic_player_heart_outline)
                }
            }
        }
    }


    private fun setLifecycleState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
        Log.d(TAG, "setLifecycleState: $state")
    }

    private fun setPlayInfo(info: StandardSongData) {
        Log.d(TAG, "setPlayInfo: ${info.name} ${info.artists?.get(0)?.name}")
        val art = if (info.artists != null) getSingerList(info.artists!!) else ""
        val titleStr = "${info.name}${if (art.isNotEmpty()) " - $art" else ""}"
        title?.text = titleStr
        icLike?.setImageResource(R.drawable.ic_player_heart_outline)
    }

    private fun updateProgress(pr : Long) {
        if (App.musicController.value?.isPlaying()?.value == true) {
            lyricView?.updateTime(pr)
            progressBar?.progress = pr.toInt()
        }
    }

    private fun initView(v: View) {
        v.setOnClickListener{
            val intentMain = Intent(App.context, MainActivity::class.java)
            val intentPlayer = Intent(App.context, PlayerActivity::class.java)
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                App.context.startActivities(arrayOf(intentMain, intentPlayer))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        title = v.findViewById(R.id.title)
        icLike = v.findViewById(R.id.float_like_status)
        icPlay = v.findViewById(R.id.float_play_status)
        lyricView = v.findViewById(R.id.lyricView)
        progressBar = v.findViewById(R.id.float_progress)
        lyricView?.loadLyric(EMPTY_LYRIC)

        icLike?.setColorFilter(App.context.resources.getColor(R.color.float_widget_focus_color))
        icPlay?.setColorFilter(App.context.resources.getColor(R.color.float_widget_focus_color))

        icLike?.setOnClickListener {
            App.musicController.value?.getPlayingSongData()?.value?.let {
                if (it.source == SOURCE_NETEASE) {
                    lifecycleScope.launch {
                        val liked = User.userLikeData.value?.contains(it.id?.toLong() ?: -1) ?: false
                        Api.likeSong(!liked, it.id ?: "").let { codeData ->
                            withContext(Dispatchers.Main) {
                                if (codeData?.code == 200) {
                                    icLike?.setImageResource(if (liked) R.drawable.ic_player_heart_outline else R.drawable.ic_player_heart)
                                    User.updateLikeList(User.uid)
                                } else {
                                    toast(if (liked) "取消喜欢失败" else "喜欢失败")
                                }
                            }
                        }
                    }
                }
            }
        }
        icPlay?.setOnClickListener {
            App.musicController.value?.changePlayState()
        }
        initObserver()
    }

    private fun updatePlayStatus(isPlaying: Boolean) {
        icPlay?.let { iv ->
            if (isPlaying) {
                iv.setImageResource(R.drawable.ic_player_pause)
            } else {
                iv.setImageResource(R.drawable.ic_player_play)
            }
        }
    }


    private fun getSingerList(singers: ArrayList<StandardSongData.StandardArtistData>):String {
        val buffer = StringBuffer()
        for (singer in singers) {
            if (buffer.isNotEmpty()) {
                buffer.append(" ")
            }
            buffer.append(singer.name)
        }
        return buffer.toString()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }


}