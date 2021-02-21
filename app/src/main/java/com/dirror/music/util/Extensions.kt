package com.dirror.music.util

import android.graphics.Color
import androidx.viewpager2.widget.ViewPager2

/**
 * 拓展函数
 */

/**
 * 字节数组转 16 进制字符串
 */
fun ByteArray.toHex(): String? {
    val stringBuilder = StringBuilder("")
    if (this.isEmpty()) {
        return null
    }
    for (element in this) {
        val v = element.toInt() and 0xFF
        val hv = Integer.toHexString(v)
        if (hv.length < 2) {
            stringBuilder.append(0)
        }
        stringBuilder.append(hv)
    }
    return stringBuilder.toString()
}

/**
 * 隐藏
 */
fun ViewPager2.hideScrollMode() {
    ViewPager2Util.changeToNeverMode(this)
}

/**
 * dp
 */
fun Int.dp(): Int {
    return dp2px(this.toFloat()).toInt()
}

/**
 * 判断是否是中文字符
 */
fun Char.isChinese(): Boolean {
    val unicodeBlock = Character.UnicodeBlock.of(this)
    if (unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
        || unicodeBlock == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) // 中日韩象形文字
    {
        return true
    }
    return false
}

fun Long.parseSize(): String {
    val source = this.toDouble()
    if (this < 1_000) {
        return "${String.format("%.2f", source)} KB"
    }
    if (this < 1_000_000) {
        return "${String.format("%.2f", source / 1_000)} KB"
    }
    if (this < 1_000_000_000) {
        return "${String.format("%.2f", source / 1_000_000)} MB"
    }
    return "${String.format("%.2f", source / 1_000_000_000)} GB"
}

/**
 * 混合颜色
 * [color] 是要混合的颜色
 */
fun Int.colorMix(color: Int): Int {
    val red = (Color.red(this) + Color.red(color)) / 2
    val green = (Color.green(this) + Color.green(color)) / 2
    val blue = (Color.blue(this) + Color.blue(color)) / 2
    return Color.rgb(red, green, blue)
}

fun Int.colorAlpha(alpha: Float): Int {
    val a = if (alpha in 0f..1f) {
        Color.alpha(this) * alpha
    } else {
        255
    }.toInt()
    return Color.argb(a, Color.red(this), Color.green(this), Color.blue(this))
}