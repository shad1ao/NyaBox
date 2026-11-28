package io.nekohasekai.sagernet.ktx

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Material 3 widgets wrap the inflation context in [androidx.appcompat.view.ContextThemeWrapper].
 * Casting that wrapper to an activity throws. Walk [ContextWrapper.baseContext] until the match.
 */
fun Context.findActivity(): Activity? = findContext()

inline fun <reified T : Any> Context.findContext(): T? =
    unwrap(this, { (it as? ContextWrapper)?.baseContext }) { it as? T }

fun <N, T> unwrap(start: N?, next: (N) -> N?, match: (N) -> T?): T? {
    var current = start
    var depth = 0
    while (current != null && depth++ < 32) {
        match(current)?.let { return it }
        val parent = next(current)
        if (parent === current) return null
        current = parent
    }
    return null
}
