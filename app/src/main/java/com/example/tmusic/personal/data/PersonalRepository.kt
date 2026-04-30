package com.example.tmusic.personal.data

import com.example.tmusic.TApplication
import com.example.tmusic.localMusicList.data.room.MusicEntity
import org.json.JSONArray
import org.json.JSONObject

object PersonalRepository {
    private val mk = TApplication.mmkv

    init {
        // Initialize start time if not exists
        if (mk.decodeLong("app_start_time", 0L) == 0L) {
            mk.encode("app_start_time", System.currentTimeMillis())
        }
    }

    fun getAppStartDays(): Int {
        val startTime = mk.decodeLong("app_start_time", System.currentTimeMillis())
        val diff = System.currentTimeMillis() - startTime
        return (diff / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    fun addFocusCount() {
        val current = mk.decodeInt("focus_count", 0)
        mk.encode("focus_count", current + 1)
    }

    fun getFocusCount(): Int = mk.decodeInt("focus_count", 0)

    fun addFocusDuration(seconds: Int) {
        val current = mk.decodeLong("focus_duration", 0L)
        mk.encode("focus_duration", current + seconds)
    }

    fun getFocusDurationHours(): Int {
        val totalSeconds = mk.decodeLong("focus_duration", 0L)
        return (totalSeconds / 3600).toInt()
    }

    fun addPlayDuration(seconds: Int) {
        val current = mk.decodeLong("total_play_seconds", 0L)
        mk.encode("total_play_seconds", current + seconds)
    }

    fun getTotalPlayDurationHours(): Int {
        val totalSeconds = mk.decodeLong("total_play_seconds", 0L)
        return (totalSeconds / 3600).toInt()
    }

    fun recordSongPlay(music: MusicEntity) {
        // Increment play count
        val countKey = "play_count_${music.id}"
        val newCount = mk.decodeInt(countKey, 0) + 1
        mk.encode(countKey, newCount)

        // Check if it's the most played
        val mostPlayedId = mk.decodeLong("most_played_id", -1L)
        val mostPlayedCount = mk.decodeInt("most_played_count", 0)
        if (newCount > mostPlayedCount) {
            mk.encode("most_played_id", music.id)
            mk.encode("most_played_count", newCount)
            mk.encode("most_played_title", music.title)
            mk.encode("most_played_cover", music.albumArt ?: "")
        }

        // Add to history
        val historyStr = mk.decodeString("play_history", "[]")
        try {
            val jsonArray = JSONArray(historyStr)
            val newArray = JSONArray()
            val musicObj = JSONObject().apply {
                put("id", music.id)
                put("title", music.title)
                put("artist", music.artist)
                put("uri", music.uri)
                put("duration", music.duration)
                put("albumArt", music.albumArt ?: "")
                music.lyrics?.let { put("lyrics", it) }
            }
            newArray.put(musicObj)
            
            // Keep up to 20 unique history items
            var count = 1
            for (i in 0 until jsonArray.length()) {
                if (count >= 20) break
                val obj = jsonArray.getJSONObject(i)
                if (obj.getLong("id") != music.id) {
                    newArray.put(obj)
                    count++
                }
            }
            mk.encode("play_history", newArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMostPlayedSong(): MostPlayedSong? {
        val count = mk.decodeInt("most_played_count", 0)
        if (count == 0) return null
        return MostPlayedSong(
            title = mk.decodeString("most_played_title", "") ?: "",
            coverPath = mk.decodeString("most_played_cover", "") ?: "",
            playCount = count
        )
    }

    fun getPlayHistory(): List<MusicEntity> {
        val historyStr = mk.decodeString("play_history", "[]")
        val result = mutableListOf<MusicEntity>()
        try {
            val jsonArray = JSONArray(historyStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val music = MusicEntity(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    artist = obj.getString("artist"),
                    uri = obj.getString("uri"),
                    duration = obj.getLong("duration"),
                    albumArt = obj.optString("albumArt").takeIf { it.isNotEmpty() },
                    lyrics = obj.optString("lyrics").takeIf { it.isNotEmpty() }
                )
                result.add(music)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

data class MostPlayedSong(
    val title: String,
    val coverPath: String,
    val playCount: Int
)
