package com.example.tmusic.utils

import com.example.tmusic.localMusicList.data.room.MusicEntity
import java.text.Collator
import java.util.Locale

object MusicSortUtils {

    /**
     * 获取字符串的排序类别
     * 0: 英文字母或中文字符 (优先)
     * 1: 数字、符号、日文等其他字符 (靠后)
     */
    private fun getSortCategory(str: String): Int {
        if (str.isEmpty()) return 1
        
        // 我们只看第一个有效字符来决定分类
        val firstChar = str.firstOrNull { !it.isWhitespace() } ?: return 1

        // 英文字母
        if (firstChar in 'a'..'z' || firstChar in 'A'..'Z') {
            return 0
        }

        // 中文字符 (CJK Unified Ideographs)
        val ub = Character.UnicodeBlock.of(firstChar)
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C ||
            ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D ||
            ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
            ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT) {
            return 0
        }

        // 其他字符 (数字、符号、日文等)
        return 1
    }

    private val chinaCollator = Collator.getInstance(Locale.CHINA)

    /**
     * 按歌曲名称排序的比较器
     * 最开始是A到Z（包含拼音的A-Z），然后是特殊符号（日语，数字，符号等）
     */
    val musicNameComparator = Comparator<MusicEntity> { m1, m2 ->
        val t1 = m1.title
        val t2 = m2.title
        
        val cat1 = getSortCategory(t1)
        val cat2 = getSortCategory(t2)
        
        if (cat1 != cat2) {
            cat1.compareTo(cat2) // 0 排在 1 前面
        } else {
            // 同一个类别内，使用中文拼音规则进行排序
            chinaCollator.compare(t1, t2)
        }
    }
}
