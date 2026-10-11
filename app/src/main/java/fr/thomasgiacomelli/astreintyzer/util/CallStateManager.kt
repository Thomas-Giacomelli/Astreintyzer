package fr.thomasgiacomelli.astreintyzer.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object CallStateManager {
    private const val PREFS_NAME = "call_state_prefs"
    private const val KEY_WAS_OFFHOOK = "was_offhook"
    private const val KEY_CALL_START_TIME = "call_start_time"
    private const val KEY_LAST_CALL_TIMESTAMP = "last_call_timestamp"
    private const val KEY_LAST_CALL_DURATION_SEC = "last_call_duration_sec"
    private const val KEY_DEBUG_MODE = "debug_mode"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun onCallStarted(context: Context) {
        val now = System.currentTimeMillis()
        getPrefs(context).edit {
            putBoolean(KEY_WAS_OFFHOOK, true)
            putLong(KEY_CALL_START_TIME, now)
        }
    }

    fun onCallEnded(context: Context): Pair<Long, Long> {
        val prefs = getPrefs(context)
        val startTime = prefs.getLong(KEY_CALL_START_TIME, System.currentTimeMillis())
        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - startTime) / 1000L).coerceAtLeast(1L)

        prefs.edit {
            putBoolean(KEY_WAS_OFFHOOK, false)
            putLong(KEY_LAST_CALL_TIMESTAMP, startTime)
            putLong(KEY_LAST_CALL_DURATION_SEC, durationSec)
        }

        return Pair(startTime, durationSec)
    }

    fun wasOffhook(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WAS_OFFHOOK, false)
    }

    fun getLastCallDetails(context: Context): Pair<Long, Long> {
        val prefs = getPrefs(context)
        val timestamp = prefs.getLong(KEY_LAST_CALL_TIMESTAMP, System.currentTimeMillis())
        val duration = prefs.getLong(KEY_LAST_CALL_DURATION_SEC, 0L)
        return Pair(timestamp, duration)
    }

    fun isDebugModeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DEBUG_MODE, false)
    }

    fun setDebugModeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_DEBUG_MODE, enabled)
        }
    }
}
