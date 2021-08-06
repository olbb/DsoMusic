package com.dirror.music.util

import android.util.Log
import com.dirror.music.api.API_LOGIN
import com.dirror.music.api.API_NEW
import com.dirror.music.api.CloudMusicApi
import com.dirror.music.data.*
import com.dirror.music.manager.User
import com.dirror.music.music.compat.CompatSearchData
import com.dirror.music.music.compat.compatSearchDataToStandardPlaylistData
import com.dirror.music.music.netease.Playlist
import com.dirror.music.music.netease.data.CodeData
import com.dirror.music.music.netease.data.LyricData
import com.dirror.music.music.qq.SearchSong
import com.dirror.music.music.standard.data.*
import com.dso.ext.averageAssignFixLength
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Api {

    private const val TAG = "API"

    private const val SPLIT_PLAYLIST_NUMBER = 1000 // 切割歌单
    private const val CHEATING_CODE = -460 // Cheating 错误

    suspend fun getPlayListInfo(id: Long): DetailPlaylistInnerData? {
        val params = HashMap<String, String>()
        params["id"] = id.toString()
        if (User.hasCookie) {
            params["cookie"] = User.cookie
        }
        val url = "$API_NEW/playlist/detail?id=${id}&hash=${params.hashCode()}"
        return HttpUtils.post(url, params, DetailPlaylistData::class.java)?.playlist
    }

    suspend fun getPlayList(id: Long, useCache: Boolean): PackedSongList {
        val params = HashMap<String, String>()
        params["id"] = id.toString()
        if (User.hasCookie) {
            params["cookie"] = User.cookie
        }
        val url = "$API_NEW/playlist/detail?hash=${params.hashCode()}"
        val result = HttpUtils.postWithCache(url, params, DetailPlaylistData::class.java, useCache)
        val trackIds = ArrayList<Long>()
        result?.result?.playlist?.trackIds?.forEach {
            trackId -> trackIds.add(trackId.id)
        }
        val list = ArrayList<StandardSongData>()
        if (trackIds.size > 0) {
            trackIds.averageAssignFixLength(SPLIT_PLAYLIST_NUMBER).forEach lit@ { subTrackIds ->
                Log.d(TAG, "subTrackIds size is ${subTrackIds.size}")
                val idsBuilder = StringBuilder()
                for (trackId in subTrackIds) {
                    if (idsBuilder.isNotEmpty()) {
                        idsBuilder.append(",")
                    }
                    idsBuilder.append(trackId)
                }
                val ids = idsBuilder.toString()
                val data = HttpUtils.postWithCache("$API_NEW/song/detail?hash=${ids.hashCode()}",
                    Utils.toMap("ids", ids), CompatSearchData::class.java, useCache)
//                val data = HttpUtils.get("$API_NEW/song/detail?ids=${ids}", CompatSearchData::class.java)
                data?.result?.apply {
                    if (code == CHEATING_CODE) {
                        toast("-460 Cheating")
                        // 发生了欺骗立刻返回
                        return@lit
                    } else {
                        Log.i(TAG, "get result ${songs.size}")
                        list.addAll(compatSearchDataToStandardPlaylistData(this))
                    }
                }
            }

        }
        Log.d(TAG, "get playlist id $id, size:${list.size} , origin size:${trackIds.size}")
        return PackedSongList(list, result?.isCache?:false, result?.result?.playlist)
    }

    suspend fun searchMusic(keyword:String, type:SearchType): StandardSearchResult? {
        val url = "$API_NEW/cloudsearch?keywords=$keyword&limit=100&type=${SearchType.getSearchTypeInt(type)}"
        val result = HttpUtils.get(url, NeteaseSearchResult::class.java)
        return result?.result?.toStandardResult()
    }

    suspend fun getAlbumSongs(id:Long): StandardAlbumPackage? {
        val url = "$API_NEW/album?id=${id}"
        HttpUtils.get(url, NeteaseAlbumResult::class.java)?.let {
            return StandardAlbumPackage(it.album.switchToStandard(), it.switchToStandardSongs())
        }
        return null
    }

    suspend fun getSingerSongs(id: Long): StandardSingerPackage? {
        val songs = ArrayList<StandardSongData>()
        var result: ArtistsSongs?
        do {
            val url = "$API_NEW/artist/songs?id=$id&offset=${songs.size}"
            result = HttpUtils.get(url, ArtistsSongs::class.java, true)
            result?.let {
//                Log.d(TAG, "getSingerSongs result${result.songs.size} ")
                songs.addAll(it.switchToStandardSongs())
            }
        } while (result?.more == true && result.songs.isNotEmpty())

        HttpUtils.get("$API_NEW/artist/detail?id=$id", ArtistInfoResult::class.java, true)?.data?.artist?.let {
            return StandardSingerPackage(it.switchToStandardSinger(), songs)
        }
        return null
    }

    suspend fun getOtherCPSong(song: StandardSongData): StandardSongData? {
        val songName = song.name?.replace(Regex("（.*）"), "")?.trim()?:""

        searchFromKuwo(songName)?.abslist?.let {
            for (res in it) {
                if (res.NAME == song.name || res.NAME.contains(songName)) {
                    song.artists?.let { artists ->
                        var checkSingerCount = 0
                        for (singer in artists) {
                            if (res.ARTIST == singer.name) {
                                return res.switchToStandard()
                            } else if (singer.name != null) {
                                if (res.ARTIST.contains(singer.name)) {
                                    checkSingerCount++
                                } else {
                                    break
                                }
                            }
                        }
                        if (checkSingerCount == song.artists.size) return res.switchToStandard()
                    }

                }
            }
        }
        val artistName = song.artists?.first()?.name
        searchFromQQ("$songName $artistName")?.data?.song?.list?.let {
            for (res in it) {
                if (res.songname == song.name || res.songname.contains(songName)) {
                    val nameBuffer = StringBuffer()
                    for (singer in res.singer) {
                        singer.name?.let { singerName -> nameBuffer.append(singerName) }
                    }
                    val names = nameBuffer.toString()
                    var checkSingerCount = 0
                    song.artists?.forEach forArtists@ { artist->
                        artist.name?.let { name ->
                            if (names.contains(name)) {
                                checkSingerCount++
                            } else {
                                return@forArtists
                            }
                        }
                    }
                    if (checkSingerCount == song.artists?.size) return res.switchToStandard()
                }
            }
        }
        return null
    }

    private suspend fun searchFromQQ(keywords: String): SearchSong.QQSearch? {
        val url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?aggr=1&cr=1&flag_qc=0&p=1&n=20&w=${keywords}"
        HttpUtils.get(url, String::class.java)?.let {
            var response = it.replace("callback(", "")
            if (response.endsWith(")")) {
                response = response.substring(0, response.lastIndex)
            }
            try {
                return Gson().fromJson(response, SearchSong.QQSearch::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private suspend fun searchFromKuwo(keywords: String): com.dirror.music.music.kuwo.SearchSong.KuwoSearchData? {
        val url = "http://search.kuwo.cn/r.s?songname=${keywords}&ft=music&rformat=json&encoding=utf8&rn=30&callback=song&vipver=MUSIC_8.0.3.1"
        HttpUtils.get(url, String::class.java)?.let {
            var string = it
            // 适配 JSON
            string = string.replace("try{var jsondata=", "")
            string = string.replace(
                "\n" +
                        "; song(jsondata);}catch(e){jsonError(e)}", ""
            )
            string = string.replace("\'", "\"")
            string = string.replace("&nbsp;", " ")
            try {
                return  Gson().fromJson(string, com.dirror.music.music.kuwo.SearchSong.KuwoSearchData::class.java)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
        return null
    }

    suspend fun getLyricUrl(id : String) : LyricData? {
        return HttpUtils.get("${CloudMusicApi.LYRIC}?id=$id", LyricData::class.java)
    }

    suspend fun likeSong(like: Boolean, id: String): CodeData? {
        val params = Utils.toMap("id", id, "cookie", User.cookie, "like", like.toString())
        return HttpUtils.post("$API_LOGIN/like?timestamp=${Date().time}", params, CodeData::class.java)
    }

    suspend fun subscribePlaylist(like: Boolean, id: String) : CodeData? {
        val params = Utils.toMap("id", id, "cookie", User.cookie, "t", if (like) "1" else "2")
        return HttpUtils.post("$API_LOGIN/playlist/subscribe?timestamp=${Date().time}", params, CodeData::class.java)
    }

    suspend fun getLoginKey(): NeteaseGetKey? {
        return HttpUtils.get("${getLoginUrl()}/login/qr/key?timestamp=${Date().time}", NeteaseGetKey::class.java)
    }

    suspend fun getLoginQRCode(key: String): NeteaseQRCodeResult? {
        return HttpUtils.get("${getLoginUrl()}/login/qr/create?key=$key&qrimg=1&timestamp=${Date().time}", NeteaseQRCodeResult::class.java)
    }

    suspend fun checkLoginResult(key: String): NeteaseLoginResult? {
        return HttpUtils.get("${getLoginUrl()}/login/qr/check?key=$key&timestamp=${Date().time}", NeteaseLoginResult::class.java)
    }

    suspend fun getUserInfo(cookie: String): NeteaseUserInfo? {
        return HttpUtils.post("${getLoginUrl()}/user/account", Utils.toMap("cookie", cookie) , NeteaseUserInfo::class.java)
    }

    private fun getLoginUrl() :String {
        return API_LOGIN
    }

}