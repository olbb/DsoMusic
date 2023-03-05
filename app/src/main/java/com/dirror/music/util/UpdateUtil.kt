package com.dirror.music.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import com.dirror.music.ui.dialog.UpdateDialog
import com.dirror.music.util.http.SSLTools.supportTLS
import com.google.gson.Gson
import com.umeng.commonsdk.statistics.SdkVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@Keep
object UpdateUtil {

    const val TAG = "UpgradeUtil"

    /**
     * 检查新版本
     * 传入 [activity]，[showLastedToast] 开启表示显示如果是最新版或是获取数据错误时弹出 Toast
     */
    fun checkNewVersion(activity: Activity, showLastedToast: Boolean) {
        getServerVersion({ updateData ->
            runOnMainThread {
                if ("custom" !in getVisionName()) {
                    val version = getVisionCode()
                    Log.i(TAG, "当前版本：${version}, 服务器版本:${updateData.code}")
                    if (updateData.code > version) {
                        // 有新版
                        UpdateDialog(activity, updateData).show()
                    } else {
                        if (showLastedToast) {
                            toast("已是最新版本\n服务器版本：${updateData.name}(${updateData.code})")
                        }
                    }
                } else {
                    if (showLastedToast) {
                        toast("定制版不支持检查更新，请访问本应用 Github 页面查看更新")
                    }
                }
            }
        }, {
            if (showLastedToast) {
                toast("获取服务器版本信息失败")
            }
        })
    }

    /**
     * 检查服务器版本
     */
    private fun getServerVersion(success: (UpdateData) -> Unit, failure: () -> Unit) {
        val url = "https://catfun.ml/tools/check_up.json"
        MagicHttp.OkHttpManager().newGet(url, {
            // 成功
            try {
                success.invoke(Gson().fromJson(it, UpdateData::class.java))
            } catch (e: Exception) {
                failure.invoke()
            }
        }, {

        })
    }

    @Keep
    data class UpdateData(
        val name: String,
        val code: Int,
        val content: String,
        val url: String, // 下载链接
        var tagVersion: Int?,
    )


    // 下载并安装 APK
    suspend fun downloadAndInstallApk(context: Context, apkUrl: String) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "start downloadAndInstallApk:$apkUrl")

            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(3, TimeUnit.SECONDS)
                    .supportTLS()
                    .build()
                val request = Request.Builder()
                    .url(apkUrl)
                    .build()

                val response = client.newCall(request).execute()
                val inputStream = response.body()?.byteStream()
                if (inputStream == null) {
                    toast("下载失败")
                    return@withContext
                }

                val apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "app-update.apk")
                val outputStream = FileOutputStream(apkFile)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()

                Log.i(TAG, "download url finished. path is ${apkFile.absolutePath}")
                toast("下载完成，开始安装...")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        context.packageName + ".FileProvider",
                        apkFile
                    )
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(contentUri, "application/vnd.android.package-archive")
                    }
                    Log.d(TAG, "content url is:$contentUri" + ",intent url:${installIntent.data}")
                    Log.i(TAG, "pending to start intent $installIntent")
                    context.startActivity(installIntent)
                } else {
                    val contentUri = Uri.fromFile(apkFile)
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setDataAndType(contentUri, "application/vnd.android.package-archive")
                    }
                    context.startActivity(installIntent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                toast("下载失败${e.message}")
            }
        }
    }

}