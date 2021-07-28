package com.dirror.music.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AlwaysFocusTextView(context: Context, attrs: AttributeSet): AppCompatTextView(context, attrs) {

    override fun isFocused(): Boolean {
        return true
    }
}