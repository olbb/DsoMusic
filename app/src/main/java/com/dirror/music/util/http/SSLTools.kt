package com.dirror.music.util.http

import android.os.Build
import android.util.Log
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.security.Provider
import java.security.Security
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author JuanLv created at 2023/3/3
 * olbbme@gmail.com
 */
object SSLTools {

    private val chiperes: List<ConnectionSpec> by lazy {
        listOf(
            ConnectionSpec.RESTRICTED_TLS,
            ConnectionSpec.COMPATIBLE_TLS,
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.CLEARTEXT // Support Http
        )
    }

    private val tm: X509TrustManager by lazy {
        Conscrypt.getDefaultX509TrustManager()
    }

    // Init Conscrypt
    private val conscrypt: Provider by lazy {
        Conscrypt.newProvider()
    }

    private val sslContext: SSLContext by lazy {
        Log.i(TAG, "init started")
        val context = SSLContext.getInstance("TLS", conscrypt)
        // Add as provider
        Security.insertProviderAt(conscrypt, 1)
        context.init(null, arrayOf<TrustManager>(tm), null)
        Log.i(TAG, "init finished")
        context
    }

    const val TAG = "SSLTools"

    fun OkHttpClient.Builder.supportTLS():OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this
        }
        Log.d(TAG, "set supportTLS")
        connectionSpecs(chiperes)
        sslSocketFactory(
            InternalSSLSocketFactory(sslContext.socketFactory),
            tm
        )
        return  this
    }
}