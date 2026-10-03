package com.qingjian.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "青简 Android\n\n第一版使用青简 Rust Core。\n更多输入方案与设置将在后续版本加入。"
            textSize = 18f
            setPadding(48, 64, 48, 48)
        })
    }
}
