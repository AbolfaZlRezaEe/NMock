package me.abolfazl.nmock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager

class GPSBroadcastReceiver : BroadcastReceiver() {
    private var callBack: (() -> Unit)? = null
    private var receiverRegistered = false

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action != null) {
            if (intent.action.equals("android.location.PROVIDERS_CHANGED")) {
                callBack?.invoke()
            }
        }
    }

    fun setLocationStateChangeListener(callBack: () -> Unit) {
        this.callBack = callBack
    }

    fun registerReceiver(context: Context) {
        if (!receiverRegistered) {
            context.registerReceiver(
                this,
                IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
            receiverRegistered = true
        }
    }

    fun unregisterReceiver(context: Context) {
        if (receiverRegistered) {
            context.unregisterReceiver(this)
            receiverRegistered = false
        }
    }
}