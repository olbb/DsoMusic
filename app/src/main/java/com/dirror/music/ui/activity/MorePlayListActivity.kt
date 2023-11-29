package com.dirror.music.ui.activity

import android.content.Intent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.dirror.music.adapter.PlaylistAdapter
import com.dirror.music.data.SearchType
import com.dirror.music.databinding.ActivityMorePlaylistBinding
import com.dirror.music.music.standard.data.StandardPlaylist
import com.dirror.music.ui.base.BaseActivity
import com.dirror.music.ui.playlist.SongPlaylistActivity
import com.dirror.music.ui.playlist.TAG_NETEASE
import com.dirror.music.util.Api
import com.dirror.music.util.getStatusBarHeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 通过歌曲id查找歌单
 */
class MorePlayListActivity : BaseActivity() {

    companion object {
        const val EXTRA_SONG_ID = "extra_song_id"
        const val EXTRA_SONG_NAME = "extra_song_name"
    }

    lateinit var binding: ActivityMorePlaylistBinding
    private var songId:String? = null

    override fun initBinding() {
        binding = ActivityMorePlaylistBinding.inflate(layoutInflater)
        miniPlayer = binding.miniPlayer
        setContentView(binding.root)
    }

    override fun initView() {
        super.initView()
        (binding.titleBar.layoutParams as ConstraintLayout.LayoutParams).apply{
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin = getStatusBarHeight(window, this@MorePlayListActivity)
        }
        binding.ivSearch.setOnClickListener { finish() }
    }

    override fun initData() {
        super.initData()
        intent.getStringExtra(EXTRA_SONG_ID)?.let {
            GlobalScope.launch {
                Api.getSimiPlayList(it)?.playlists?.let {
                    val list = it.map { playlist ->
                        playlist.switchToStandSearchPlaylist()
                    }
                    withContext(Dispatchers.Main) {
                        initPlaylist(list)
                    }
                }
            }

        }
    }

    private fun initPlaylist(playlists:List<StandardPlaylist>) {
        binding.rvPlaylist.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylist.adapter = PlaylistAdapter {
            val intent = Intent(this, SongPlaylistActivity::class.java)
            intent.putExtra(SongPlaylistActivity.EXTRA_TAG, TAG_NETEASE)
            intent.putExtra(SongPlaylistActivity.EXTRA_ID, it.id.toString())
            startActivity(intent)
        }.apply {
            submitList(playlists)
        }
    }


}