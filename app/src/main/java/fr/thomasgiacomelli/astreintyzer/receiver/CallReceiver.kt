package fr.thomasgiacomelli.astreintyzer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import fr.thomasgiacomelli.astreintyzer.util.AlarmScheduler
import fr.thomasgiacomelli.astreintyzer.util.CallStateManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                when (stateStr) {
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        CallStateManager.onCallStarted(context)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        if (CallStateManager.wasOffhook(context)) {
                            val (timestamp, durationSec) = CallStateManager.onCallEnded(context)
                            AlarmScheduler.scheduleNotification(
                                context = context,
                                delayMillis = 30_000L,
                                callTimestamp = timestamp,
                                callDurationSeconds = durationSec
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallReceiver", "Erreur lors du traitement de l'événement d'appel", e)
        }
    }
}
