package com.dirror.music.music.netease

import android.content.Context
import com.dirror.music.music.netease.data.TopListData
import com.dirror.music.util.Api
import com.dirror.music.util.MagicHttp
import com.google.gson.Gson

/**
 * 排行榜
 */
object TopList {

    private val API = "${Api.getDefaultApi()}/toplist/detail"

    fun getTopList(context: Context, success: (TopListData) -> Unit, failure: () -> Unit) {
        MagicHttp.OkHttpManager().getByCache(context, API, {
            try {
                val topListData = Gson().fromJson(it, TopListData::class.java)
                if (topListData.code == 200) {
                    success.invoke(topListData)
                }
            } catch (e: Exception) {
                failure.invoke()
            }
        }, {
            failure.invoke()
        })
    }

}