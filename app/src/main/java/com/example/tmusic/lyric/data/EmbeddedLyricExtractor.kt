package com.example.tmusic.lyric.data

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

object EmbeddedLyricExtractor {
    
    init {
        // Disable jaudiotagger logging to prevent console spam
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }

    fun extractLyrics(filePath: String?): String? {
        if (filePath.isNullOrEmpty()) return null
        try {
            val file = File(filePath)
            if (!file.exists() || !file.canRead()) return null
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: return null
            
            // Try to get standard lyrics field
            var lyrics = tag.getFirst(FieldKey.LYRICS)
            
            // Sometimes it's stored in a different format or synchronized lyrics,
            // but standard LYRICS key covers USLT in ID3v2 and SYLT if properly mapped.
            if (lyrics.isNullOrBlank()) {
                return null
            }
            
            return lyrics.trim()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
