package com.dirror.music.music.netease

import android.util.Log
import com.dirror.music.App
import com.dirror.music.manager.User
import com.dirror.music.music.dirror.SearchSong
import com.dirror.music.music.netease.data.SongUrlData
import com.dirror.music.util.Api
import com.dirror.music.util.AppConfig
import com.dirror.music.util.HttpUtils
import com.dirror.music.util.MagicHttp
import com.google.gson.Gson
import okhttp3.FormBody

object SongUrl {

    val API = "${Api.getDefaultApi()}/song/url?id=33894312"

    fun getSongUrl(id: String): String {
        return if (SearchSong.getDirrorSongUrl(id) != "") {
            SearchSong.getDirrorSongUrl(id)
        } else {
            "https://music.163.com/song/media/outer/url?id=${id}.mp3"
        }
    }

    fun getSongUrlCookie(id: String, success: (String) -> Unit) {
        val api = Api.getDefaultApi()
        val requestBody = FormBody.Builder()
            .add("crypto", "api")
            .add("cookie", AppConfig.cookie)
            .add("withCredentials", "true")
            .add("realIP", App.realIP)
            .add("id", id)
            .build()
        MagicHttp.OkHttpManager().newPost("${api}/song/url", requestBody, {
            try {
                Log.d("SongUrl", "getSongUrlCookie result: $it")
                val songUrlData = Gson().fromJson(it, SongUrlData::class.java)
                success.invoke(songUrlData.data[0].url ?: "")
            } catch (e: Exception) {
                // failure.invoke(ErrorCode.ERROR_JSON)
            }
        }, {

        })
    }

    suspend fun getSongUrlN(id: String): String {
        val url = "${Api.getDefaultApi()}/song/url?id=$id"
        val result = HttpUtils.get(url, SongUrlData::class.java)
        return result?.data?.get(0)?.url ?: getSongUrl(id)
    }

}