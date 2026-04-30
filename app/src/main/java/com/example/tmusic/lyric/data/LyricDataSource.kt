package com.example.tmusic.lyric.data

import java.io.InputStream
import java.nio.charset.Charset

class LyricDataSource {

    fun readLyricText(inputStream: InputStream): String {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) return ""

        // Check BOM
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }

        // Heuristic for UTF-8 vs GBK
        if (isUtf8(bytes)) {
            return String(bytes, Charsets.UTF_8)
        }
        
        return String(bytes, Charset.forName("GBK"))
    }

    private fun isUtf8(bytes: ByteArray): Boolean {
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt()
            if (b >= 0) { // ASCII
                i++
                continue
            }
            
            var bytesToRead = 0
            if (b.and(0xE0) == 0xC0) bytesToRead = 1
            else if (b.and(0xF0) == 0xE0) bytesToRead = 2
            else if (b.and(0xF8) == 0xF0) bytesToRead = 3
            else return false // Invalid UTF-8 starting byte

            if (i + bytesToRead >= bytes.size) return false // Truncated

            for (j in 1..bytesToRead) {
                if (bytes[i + j].toInt().and(0xC0) != 0x80) {
                    return false // Invalid continuation byte
                }
            }
            i += bytesToRead + 1
        }
        return true
    }
}
