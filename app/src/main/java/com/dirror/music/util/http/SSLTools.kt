package com.dirror.music.util.http

import android.os.Build
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

    private lateinit var chiperes: List<ConnectionSpec>

    private lateinit var tm: X509TrustManager

    // Init Conscrypt
    private lateinit var conscrypt: Provider
    private lateinit var sslContext: SSLContext

    private var inited = false

    fun OkHttpClient.Builder.supportTLS():OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return this
        }
        if (!inited) {
            synchronized(this) {
                initIfNeed()
            }
        }
        connectionSpecs(chiperes)
        sslSocketFactory(
            InternalSSLSocketFactory(sslContext.socketFactory),
            tm
        )
        return  this
    }


    private fun initIfNeed() {
        chiperes = listOf(
            ConnectionSpec.RESTRICTED_TLS,
            ConnectionSpec.COMPATIBLE_TLS,
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.CLEARTEXT // Support Http
        )
        try {
            tm = Conscrypt.getDefaultX509TrustManager()
            conscrypt = Conscrypt.newProvider()
            sslContext = SSLContext.getInstance("TLS", conscrypt)
            // Add as provider
            Security.insertProviderAt(conscrypt, 1)
            sslContext.init(null, arrayOf<TrustManager>(tm), null)
            inited = true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }


}