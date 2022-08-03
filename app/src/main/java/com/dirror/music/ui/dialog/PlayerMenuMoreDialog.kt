package com.dirror.music.ui.dialog

import android.content.Context
import android.content.Intent
import android.view.View
import com.dirror.music.data.SearchType
import com.dirror.music.App
import com.dirror.music.databinding.DialogPlayMoreBinding
import com.dirror.music.manager.User
import com.dirror.music.music.standard.data.SOURCE_NETEASE
import com.dirror.music.music.standard.data.SOURCE_QQ
import com.dirror.music.music.standard.data.StandardSongData
import com.dirror.music.ui.activity.MorePlayListActivity
import com.dirror.music.ui.activity.PlayHistoryActivity
import com.dirror.music.ui.base.BaseBottomSheetDialog
import com.dirror.music.util.AppConfig
import com.dirror.music.ui.playlist.SongPlaylistActivity
import com.dirror.music.ui.playlist.TAG_NETEASE
import com.dirror.music.util.Api
import com.dirror.music.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerMenuMoreDialog(context: Context) : BaseBottomSheetDialog(context) {

    private val binding: DialogPlayMoreBinding = DialogPlayMoreBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
    }


    private var song: StandardSongData? = null

    override fun initView() {

        App.musicController.value?.getPlayingSongData()?.value?.let { it ->
            binding.tvSongName.text = it.name
            song = it
        }
    }

    override fun initListener() {
        binding.apply {
            // 添加到网易云我喜欢
            itemAddNeteaseFavorite.setOnClickListener {
                if (AppConfig.cookie.isEmpty()) {
                    toast("离线模式无法收藏到在线我喜欢~")
                } else {
                    song?.let {
                        when (it.source) {
                            SOURCE_NETEASE -> {
                                GlobalScope.launch {
                                    Api.likeSong(true, it.id ?: "").let { codeData ->
                                        withContext(Dispatchers.Main) {
                                            if (codeData?.code == 200) {
                                                toast("添加到我喜欢成功")
                                            } else {
                                                toast("添加到我喜欢失败")
                                            }
                                            dismiss()
                                        }
                                    }
                                }
                            }

                            else -> {
                                toast("暂不支持此音源")
                                dismiss()
                            }
                        }
                    }
                }
            }
            // 歌曲信息
            itemSongInfo.setOnClickListener {
                App.musicController.value?.getPlayingSongData()?.value?.let { it1 ->
                    SongInfoDialog(context, it1).show()
                }
                dismiss()
            }

            // 播放历史
            itemPlayHistory.setOnClickListener {
                it.context.startActivity(Intent(it.context, PlayHistoryActivity::class.java))
                dismiss()
            }

            timeClose.setOnClickListener {
                dismiss()
                TimingOffDialog(context).show()
            }
            App.musicController.value?.getPlayingSongData()?.value?.let {
//                itemSearchPlayList.visibility = if (it.source == SOURCE_NETEASE) View.VISIBLE else View.GONE
                itemViewSingerMoreSong.visibility = if (it.source == SOURCE_NETEASE &&
                        it.artists?.size == 1) View.VISIBLE else View.GONE
                itemViewSingerMoreSong.setOnClickListener{ view ->
                    it.artists?.first()?.let { singer ->
                        val intent = Intent(view.context, SongPlaylistActivity::class.java)
                        intent.putExtra(SongPlaylistActivity.EXTRA_TAG, TAG_NETEASE)
                        intent.putExtra(SongPlaylistActivity.EXTRA_ID, singer.artistId.toString())
                        intent.putExtra(SongPlaylistActivity.EXTRA_TYPE, SearchType.SINGER)
                        view.context.startActivity(intent)
                    }
                }
                itemSearchPlayList.setOnClickListener { view ->
                    val intent = Intent(view.context, MorePlayListActivity::class.java)
//                    intent.putExtra(MorePlayListActivity.EXTRA_SONG_ID, it.id)
                    intent.putExtra(MorePlayListActivity.EXTRA_SONG_NAME, it.name)
                    view.context.startActivity(intent)
                }
            }
        }
    }

}
