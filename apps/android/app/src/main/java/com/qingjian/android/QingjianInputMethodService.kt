package com.qingjian.android

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.graphics.Color
import android.graphics.Typeface
import android.content.Context
import android.widget.*
import org.json.JSONArray

class QingjianInputMethodService : InputMethodService() {
    private lateinit var root: LinearLayout
    private lateinit var preedit: TextView
    private lateinit var candidates: LinearLayout
    private var initialized = false

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 6, 8, 8); setBackgroundColor(Color.WHITE) }
        preedit = TextView(this).apply { textSize = 16f; setTextColor(Color.DKGRAY); setPadding(12, 8, 12, 8) }
        candidates = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
        root.addView(preedit, LinearLayout.LayoutParams(-1, 42))
        root.addView(candidates, LinearLayout.LayoutParams(-1, 52))
        addKeyboard()
        ensureEngine()
        refresh()
        return root
    }

    private fun ensureEngine() {
        if (initialized) return
        assets.open("qingjian/dict.tsv").use { QingjianNative.init(it.readBytes()) }
        initialized = true
    }

    private fun addKeyboard() {
        val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        rows.forEach { chars ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            chars.forEach { ch -> row.addView(key(ch.toString()) { QingjianNative.push(ch); refresh() }, weightParams()) }
            root.addView(row, LinearLayout.LayoutParams(-1, 52))
        }
        val bottom = LinearLayout(this)
        bottom.addView(key("⌫") { QingjianNative.backspace(); refresh() }, weightParams())
        bottom.addView(key("空格") { commitCandidate(0) }, weightParams(2f))
        bottom.addView(key("↵") {
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
        }, weightParams())
        root.addView(bottom, LinearLayout.LayoutParams(-1, 54))
    }

    private fun key(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label; textSize = 15f; isAllCaps = false; setOnClickListener { action() }
    }
    private fun weightParams(weight: Float = 1f) = LinearLayout.LayoutParams(0, -1, weight).apply { setMargins(2, 2, 2, 2) }

    private fun refresh() {
        preedit.text = QingjianNative.preedit()
        candidates.removeAllViews()
        val array = JSONArray(QingjianNative.query())
        for (i in 0 until minOf(array.length(), 9)) {
            val obj = array.getJSONObject(i)
            val text = obj.optString("text")
            val b = Button(this).apply { this.text = "${i + 1} $text"; textSize = 15f; setOnClickListener { commitCandidate(i) } }
            candidates.addView(b, LinearLayout.LayoutParams(0, -1, 1f))
        }
    }

    private fun commitCandidate(index: Int) {
        val text = QingjianNative.commit(index)
        if (text.isNotEmpty()) currentInputConnection?.commitText(text, 1)
        refresh()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) { super.onStartInput(attribute, restarting); if (initialized) QingjianNative.clear() }
    override fun onFinishInput() { QingjianNative.clear(); super.onFinishInput() }
}
