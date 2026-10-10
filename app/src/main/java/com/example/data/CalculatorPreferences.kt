package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.HistoryItem
import org.json.JSONArray
import org.json.JSONObject

class CalculatorPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("super_calc_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MEMORY = "key_memory"
        private const val KEY_HISTORY = "key_history"
        private const val KEY_IS_DEG = "key_is_deg"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_THEME_MODE = "key_theme_mode" // "DARK", "LIGHT", "CYBER"
    }

    var memory: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_MEMORY, java.lang.Double.doubleToLongBits(0.0)))
        set(value) = prefs.edit().putLong(KEY_MEMORY, java.lang.Double.doubleToLongBits(value)).apply()

    var isDeg: Boolean
        get() = prefs.getBoolean(KEY_IS_DEG, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_DEG, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "CYBER") ?: "CYBER"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    fun saveHistory(history: List<HistoryItem>) {
        val array = JSONArray()
        // Save max 50 items
        history.take(50).forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun loadHistory(): List<HistoryItem> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<HistoryItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        expression = obj.getString("expression"),
                        result = obj.getString("result"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // ignore corrupted
        }
        return list
    }
}
