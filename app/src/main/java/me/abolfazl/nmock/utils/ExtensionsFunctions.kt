package me.abolfazl.nmock.utils

import android.app.ActivityManager
import android.content.Context
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun <T : RecyclerView.ViewHolder> T.setupListeners(
    onLongClick: (position: Int) -> Unit,
    onClick: (position: Int) -> Unit
): T {
    itemView.setOnLongClickListener {
        onLongClick.invoke(adapterPosition)
        return@setOnLongClickListener false
    }
    itemView.setOnClickListener {
        onClick.invoke(adapterPosition)
    }
    return this
}

fun String.changeStringTo(prefix: String): String {
    return "$prefix $this"
}

fun AppCompatActivity.showSnackBar(
    @NonNull message: String,
    @NonNull rootView: View,
    @BaseTransientBottomBar.Duration duration: Int,
    actionText: String? = null,
    actionListener: View.OnClickListener? = null
) {
    val snackBar = Snackbar.make(
        rootView, message, duration
    )
    if (actionText != null && actionListener != null) {
        snackBar.setAction(actionText, actionListener)
    }
    snackBar.show()
}

fun Fragment.showSnackBar(
    @NonNull message: String,
    @NonNull rootView: View,
    @BaseTransientBottomBar.Duration duration: Int,
    actionText: String? = null,
    actionListener: View.OnClickListener? = null
) {
    val snackBar = Snackbar.make(
        rootView, message, duration
    )
    if (actionText != null && actionListener != null) {
        snackBar.setAction(actionText, actionListener)
    }
    snackBar.show()
}

fun<T> AppCompatActivity.isServiceStillRunning(service: Class<T>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    manager.getRunningServices(Int.MAX_VALUE).forEach { serviceInfo ->
        if (service::class.java.name.equals(serviceInfo.service.className))
            return true
    }
    return false
}