package io.nekohasekai.sagernet.widget

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import com.google.android.material.color.MaterialColors
import io.nekohasekai.sagernet.R

/** Puts the toolbar navigation icon on a 40dp tonal circle, matching Material 3 back affordances. */
fun Toolbar.installTonalNavigationButton() {
    if (getTag(R.id.tonal_nav) == true) return
    setTag(R.id.tonal_nav, true)
    val apply = Runnable { applyTonalNavigationButton() }
    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> apply.run() }
    post(apply)
}

private fun Toolbar.applyTonalNavigationButton() {
    if (navigationIcon == null) return
    val button = (0 until childCount)
        .map { getChildAt(it) }
        .filterIsInstance<ImageButton>()
        .firstOrNull() ?: return
    if (button.width == 0 || button.height == 0) {
        button.post { applyTonalNavigationButton() }
        return
    }
    val key = button.width to button.height
    if (button.getTag(R.id.tonal_nav) == key) return
    button.setTag(R.id.tonal_nav, key)
    val circle = (40 * resources.displayMetrics.density).toInt()
    val insetH = ((button.width - circle) / 2).coerceAtLeast(0)
    val insetV = ((button.height - circle) / 2).coerceAtLeast(0)
    val fill = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(
            MaterialColors.getColor(
                this@applyTonalNavigationButton,
                com.google.android.material.R.attr.colorSurfaceContainerHigh,
            )
        )
    }
    val ripple = MaterialColors.getColor(
        this,
        com.google.android.material.R.attr.colorOnSurface,
    )
    button.background = RippleDrawable(
        ColorStateList.valueOf(ripple and 0x00FFFFFF or 0x33000000),
        InsetDrawable(fill, insetH, insetV, insetH, insetV),
        null,
    )
}
