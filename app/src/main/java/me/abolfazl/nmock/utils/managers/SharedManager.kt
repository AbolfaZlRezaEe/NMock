package me.abolfazl.nmock.utils.managers

import android.content.SharedPreferences
import me.abolfazl.nmock.utils.SharedParametersType

object SharedManager {

    fun putLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: Long
    ) {
        sharedPreferences
            .edit()
            .putLong(key, value)
            .apply()
    }

    fun getLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: Long
    ): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun getInt(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: Int
    ): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putInt(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: Int
    ) {
        sharedPreferences
            .edit()
            .putInt(key, value)
            .apply()
    }

    fun putString(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: String?
    ) {
        sharedPreferences
            .edit()
            .putString(key, value)
            .apply()
    }

    fun getString(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: String?
    ): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun deleteLong(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String
    ) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun putBoolean(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        value: Boolean
    ) {
        sharedPreferences
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getBoolean(
        sharedPreferences: SharedPreferences,
        @SharedParametersType key: String,
        defaultValue: Boolean
    ): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

}