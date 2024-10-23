package com.dirror.music.util.http

import android.os.Build
import android.util.Log
import com.dirror.music.App
import com.dirror.music.R
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import org.conscrypt.Conscrypt
import java.io.InputStream
import java.security.KeyStore
import java.security.Provider
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
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

    private val tms: Array<TrustManager> by lazy {
        val inputStream: InputStream =
            App.context.resources.openRawResource(R.raw.custom_root_certificate)
        val cf = CertificateFactory.getInstance("X.509")
        val cert: X509Certificate = cf.generateCertificate(inputStream) as X509Certificate
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.load(null, null)
        keystore.setCertificateEntry("custom_root_certificate", cert)
        tmf.init(keystore)
        tmf.trustManagers
//        Conscrypt.getDefaultX509TrustManager()
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

        context.init(null, tms, null)
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
            tms[0] as X509TrustManager
        )
        return  this
    }
}