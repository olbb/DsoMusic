package com.dirror.music.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.dirror.music.App
import com.dirror.music.databinding.DialogSoundEffectBinding
import com.dirror.music.ui.base.BaseBottomSheetDialog
import com.dirror.music.util.Config
import com.dirror.music.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SoundEffectDialog(context: Context, private val activity: Activity): BaseBottomSheetDialog(context) {

    companion object{
        const val TAG = "SoundEffectDialog"
    }

    private val binding = DialogSoundEffectBinding.inflate(layoutInflater)
    // 创建一个 MutableSharedFlow 用于发送 SeekBar 事件
    private val seekBarEvents = MutableSharedFlow<Int>()
    // 定义协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var speed = 1f

    init {
        setContentView(binding.root)
    }

    @OptIn(FlowPreview::class)
    override fun initView() {
        speed = App.musicController.value?.getSpeed() ?: 1f
        refreshPitch()
        coroutineScope.launch {
            seekBarEvents.debounce(300).collect {
                Log.i(TAG, "seekBarEvents progress: $it")
                App.mmkv.putFloat(Config.PLAYER_VOLUME, it/100f)
                App.musicController.value?.setVolume(it/100f)
            }
        }
    }

    override fun initListener() {
        binding.apply {
            itemEqualizer.setOnClickListener {
                try {
                    // 启动一个音频控制面板
                    // 参考 https://www.cnblogs.com/dongweiq/p/7998445.html
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                    // 传入 AudioSessionId
                    intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, App.musicController.value?.getAudioSessionId())
                    // 调用应用程序必须使用 startActivityForResult 方法启动控制面板，以便控制面板应用程序指示其包名称并用于跟踪此特定应用程序的更改
                    activity.startActivityForResult(intent, 666)
                } catch (e: Exception) {
                    toast("设备不支持均衡！")
                }
                dismiss()
            }
            ivIncreasePitch.setOnClickListener {
                App.musicController.value?.increasePitchLevel()
                refreshPitch()
            }
            ivDecreasePitch.setOnClickListener {
                App.musicController.value?.decreasePitchLevel()
                refreshPitch()
            }
            val progress = (100 * App.mmkv.decodeFloat(Config.PLAYER_VOLUME, 1.0f)).toInt()
            Log.d(TAG, "recorded progress: $progress")
            itemVolumeSeekBar.progress = progress

            itemVolumeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        coroutineScope.launch { seekBarEvents.emit(progress) }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            })
        }
    }

    /**
     * 刷新 Pitch
     */
    private fun refreshPitch() {
        binding.tvPitch.text = App.musicController.value?.getPitchLevel().toString()
    }

}