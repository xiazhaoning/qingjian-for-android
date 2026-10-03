package com.qingjian.android

object QingjianNative {
    init { System.loadLibrary("qingjian_android") }
    external fun init(dictionary: ByteArray)
    external fun push(ch: Char)
    external fun backspace(): Boolean
    external fun clear()
    external fun query(): String
    external fun preedit(): String
    external fun commit(index: Int): String
}
