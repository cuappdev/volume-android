package com.cornellappdev.volume.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Class responsible for directly interacting with the SharedPreferences, where device-persistant
 * information is stored.
 */
class PrefUtils {

    companion object {
        const val FOLLOWING_KEY: String = "following"
        const val FIRST_START_KEY: String = "firstStart"
        const val SAVED_ARTICLES_KEY: String = "savedArticles"
        private var singleton: PrefUtils? = null
        private lateinit var preferences: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor

    }

    constructor()

    @SuppressLint("CommitPrefEdits")
    constructor(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = preferences.edit()
    }

    @SuppressLint("CommitPrefEdits")
    constructor(context: Context, name: String, mode: Int) {
        preferences = context.getSharedPreferences(name, mode)
        editor = preferences.edit()
    }

    fun save(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun save(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun save(key: String, value: Set<String>) {
        editor.putStringSet(key, value).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    fun getInt(key: String, defValue: Int): Int {
        return try {
            preferences.getInt(key, defValue)
        } catch (ex: ClassCastException) {
            preferences.getString(key, defValue.toString())!!.toInt()
        }
    }

    fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    fun getStringSet(key: String, defValue: Set<String>): Set<String> {
        val stringSet = preferences.getStringSet(key, defValue)
        return stringSet ?: setOf()
    }

    fun remove(key: String) {
        editor.remove(key).apply()
    }

    private class Builder(val context: Context, val name: String?, val mode: Int) {

        fun build(): PrefUtils {
            if (mode == -1 || name == null) {
                return PrefUtils(context)
            }
            return PrefUtils(context, name, mode)
        }
    }


}