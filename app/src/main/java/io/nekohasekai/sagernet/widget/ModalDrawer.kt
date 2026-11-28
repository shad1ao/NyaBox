package io.nekohasekai.sagernet.widget

import android.content.res.ColorStateList
import android.view.Gravity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

/**
 * Compose ModalNavigationDrawer uses CornerLarge on the end edge only
 * (NavigationDrawerTokens.ContainerShape) and surfaceContainerLow.
 * NavigationView leaves that corner size at 0 until a back gesture, so the
 * sheet stays a square. Apply the shape on every layout.
 */
fun NavigationView.installModalDrawerContainer() {
    val radius = 16f * resources.displayMetrics.density
    val elevationPx = resources.displayMetrics.density
    val applyShape = {
        val gravity = (layoutParams as? DrawerLayout.LayoutParams)?.gravity ?: Gravity.START
        val opensFromLeft =
            (Gravity.getAbsoluteGravity(gravity, layoutDirection) and Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT
        val shape = ShapeAppearanceModel.builder().apply {
            setAllCornerSizes(0f)
            if (opensFromLeft) {
                setTopRightCornerSize(radius)
                setBottomRightCornerSize(radius)
            } else {
                setTopLeftCornerSize(radius)
                setBottomLeftCornerSize(radius)
            }
        }.build()
        val drawable = background as? MaterialShapeDrawable ?: MaterialShapeDrawable().also { shapeDrawable ->
            shapeDrawable.fillColor = ColorStateList.valueOf(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorSurfaceContainerLow,
                    android.graphics.Color.WHITE
                )
            )
            shapeDrawable.initializeElevationOverlay(context)
            background = shapeDrawable
        }
        drawable.shapeAppearanceModel = shape
        clipToOutline = true
        // NavigationDrawerTokens.ModalContainerElevation is level 1.
        elevation = elevationPx
    }
    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyShape() }
    post { applyShape() }
}
