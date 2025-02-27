package com.dirror.music.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.dirror.music.App
import com.dirror.music.R
import com.dirror.music.databinding.DialogSongInfoBinding
import com.dirror.music.music.dirror.SearchSong
import com.dirror.music.music.standard.data.*
import com.dirror.music.util.runOnMainThread
import com.dso.ext.toSizeFormat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class SongInfoDialog(
    context: Context,
    private val songData: StandardSongData
) : BottomSheetDialog(context, R.style.style_default_dialog) {

    private var binding: DialogSongInfoBinding = DialogSongInfoBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
        window?.setGravity(Gravity.BOTTOM)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setWindowAnimations(R.style.dialog_animation)
    }

    override fun onStart() {
        super.onStart()
        if (context.resources.configuration.orientation == 2) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            valueViewId.setValue(songData.id ?: "") // ID
            valueViewSource.setValue("未知")
            valueViewBitrate.setValue("未知")
            valueViewSize.setValue("未知")
            valueViewType.setValue("未知")
            valueViewData.setValue("未知")

            songData.let {
                when (it.source) {
                    SOURCE_NETEASE -> {
                        initSongInfo()
                    }
                    SOURCE_QQ -> {
                        if (SearchSong.getDirrorSongUrl(it.id ?: "") != "") {
                            binding.valueViewSource.setValue("Dirror 音乐")
                        } else {
                            binding.valueViewSource.setValue("QQ 音乐")
                        }
                        binding.valueViewBitrate.setValue("未知")
                        binding.valueViewSize.setValue("未知")
                        binding.valueViewType.setValue("未知")
                        initSongInfo()
                    }
                    SOURCE_LOCAL -> {
                        valueViewSource.setValue(context.getString(R.string.local_music))
                        valueViewBitrate.setValue("未知")
                        val size = songData.localInfo?.size ?: 0L
                        valueViewSize.setValue(size.toSizeFormat())
                        valueViewType.setValue("未知")
                        valueViewData.setValue(it.localInfo?.data ?: "未知")
                    }
                    SOURCE_KUWO -> {
                        binding.valueViewSource.setValue("酷我音乐")
                        initSongInfo()
                    }
                    SOURCE_BILIBILI -> {
                        binding.valueViewSource.setValue("B站")
                    }
                }

            }

        }



    }

    private fun initSongInfo() {
        App.musicController.value?.getPlayingSongData()?.value?.apply {
            if (br > 0) {
                binding.valueViewBitrate.setValue("${br / 1000} kbps")
            }
            if (fileSize > 0) {
                binding.valueViewSize.setValue(fileSize.toSizeFormat())
            }
            binding.valueViewType.setValue(type)
            binding.valueViewSource.setValue("网易云音乐")
            binding.valueViewType.setValue(type)
            binding.valueViewData.setValue(App.musicController.value?.url?.value.toString())
        }
    }

}