package com.example.tmusic.lyric.data

data class Lyric(
    val lines: List<LyricLine>,
    val offsetMs: Long = 0,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val by: String? = null
) {
    companion object {
        val EMPTY = Lyric(emptyList())
    }

    fun getLineIndexAt(timeMs: Long): Int {
        if (lines.isEmpty()) return -1
        val adjustedTime = timeMs - offsetMs
        
        var left = 0
        var right = lines.size - 1
        var result = -1

        while (left <= right) {
            val mid = left + (right - left) / 2
            if (lines[mid].startTimeMs <= adjustedTime) {
                result = mid
                left = mid + 1
            } else {
                right = mid - 1
            }
        }
        return result
    }
}
