package com.dirror.music.music.netease

import androidx.annotation.Keep
import com.dirror.music.music.standard.data.StandardPlaylist

/**
 * @author JuanLv created at 2023/7/21
 */
@Keep
data class SimiPlaylistResponse(val playlists: List<SimiPlaylist>, val code: Int)


@Keep
data class SimiPlaylist(
    val id: Long,
    val name: String,
    val coverImgUrl: String,
    val description: String,
    val trackCount: Int,
    val playCount: Long,
    val creator: Creator
) {
    fun switchToStandSearchPlaylist(): StandardPlaylist {
        return StandardPlaylist(
            id,
            name,
            coverImgUrl,
            description,
            creator.nickname,
            trackCount,
            playCount
        )
    }
}


@Keep
data class Creator(val nickname: String)

