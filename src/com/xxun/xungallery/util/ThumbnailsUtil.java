package com.xxun.xungallery.util;

import android.annotation.SuppressLint;

import java.util.HashMap;

/**
 * 保存缩略图绝对路径，缩略图工具类--这里可能并没有太大用处，可忽略。
 */
public class ThumbnailsUtil {

    @SuppressLint("UseSparseArrays")
    private static HashMap<String, String> hash = new HashMap<String, String>();

    /**
     * 返回value
     *
     * @param key
     * @return
     */
    public static String MapgetHashValue(String key, String defalt) {
        if (hash == null || !hash.containsKey(key)) return defalt;
        return hash.get(key);
    }

    /**
     */
}
