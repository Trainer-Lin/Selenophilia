package com.example.tmusic.lyric.data

import java.util.regex.Pattern
import kotlin.math.max

object LyricParser {
    // Matches [mm:ss.xx] or [mm:ss:xx] or [mm:ss.xxx]
    private val TIME_PATTERN = Pattern.compile("\\[(\\d{2,}):(\\d{2})(?:[.:](\\d{2,3}))?]")
    // Matches [tag:value]
    private val TAG_PATTERN = Pattern.compile("\\[([a-zA-Z]+):(.+)]")

    fun parse(rawLyric: String): Lyric {
        if (rawLyric.isBlank()) return Lyric.EMPTY

        val lines = mutableListOf<LyricLine>()
        var offsetMs = 0L
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var by: String? = null

        val rawLines = rawLyric.replace("\uFEFF", "").lines()

        for (line in rawLines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // Parse tags
            val tagMatcher = TAG_PATTERN.matcher(trimmedLine)
            if (tagMatcher.matches()) {
                val tag = tagMatcher.group(1)?.lowercase()
                val value = tagMatcher.group(2)?.trim()
                if (tag != null && value != null) {
                    when (tag) {
                        "offset" -> offsetMs = value.toLongOrNull() ?: 0L
                        "ti" -> title = value
                        "ar" -> artist = value
                        "al" -> album = value
                        "by" -> by = value
                    }
                }
                continue
            }

            // Parse time lines
            val timeMatcher = TIME_PATTERN.matcher(trimmedLine)
            val times = mutableListOf<Long>()
            var lastIndex = 0

            while (timeMatcher.find()) {
                val min = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val sec = timeMatcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = timeMatcher.group(3)
                val ms = parseMs(msStr)
                
                times.add(min * 60 * 1000 + sec * 1000 + ms)
                lastIndex = max(lastIndex, timeMatcher.end())
            }

            if (times.isNotEmpty()) {
                val content = trimmedLine.substring(lastIndex).trim()
                for (time in times) {
                    lines.add(LyricLine(time, content))
                }
            }
        }

        // Sort lines by start time
        lines.sortBy { it.startTimeMs }

        return Lyric(
            lines = lines,
            offsetMs = offsetMs,
            title = title,
            artist = artist,
            album = album,
            by = by
        )
    }

    private fun parseMs(msStr: String?): Long {
        if (msStr == null) return 0L
        return when (msStr.length) {
            1 -> msStr.toLongOrNull()?.times(100) ?: 0L
            2 -> msStr.toLongOrNull()?.times(10) ?: 0L
            3 -> msStr.toLongOrNull() ?: 0L
            else -> msStr.substring(0, 3).toLongOrNull() ?: 0L
        }
    }
}
