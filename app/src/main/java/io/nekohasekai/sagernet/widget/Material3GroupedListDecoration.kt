package io.nekohasekai.sagernet.widget

import android.graphics.Canvas
import androidx.core.view.updatePadding
import androidx.preference.R as PreferenceR
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.R
import android.graphics.Paint
import android.graphics.RectF
import com.google.android.material.color.MaterialColors

fun RecyclerView.applyM3GroupedCards() {
    val pad = (16 * resources.displayMetrics.density).toInt()
    updatePadding(left = pad, top = pad, right = pad)
    if (paddingBottom < pad) updatePadding(bottom = pad)
    if ((0 until itemDecorationCount).any { getItemDecorationAt(it) is Material3GroupedListDecoration }) {
        return
    }
    addItemDecoration(
        Material3GroupedListDecoration(this) { parent, position ->
            val type = parent.adapter?.getItemViewType(position) ?: return@Material3GroupedListDecoration false
            type == PreferenceR.layout.preference_category_material ||
                type == PreferenceR.layout.preference_category ||
                type == R.layout.preference_category_material
        }
    )
}

/**
 * Draws a light rounded card behind each run of rows, with a hairline between rows.
 * Category rows (or any position [isBreak] reports) stay on the canvas and split groups.
 */
class Material3GroupedListDecoration(
    view: android.view.View,
    private val isBreak: (RecyclerView, Int) -> Boolean,
) : RecyclerView.ItemDecoration() {

    private val density = view.resources.displayMetrics.density
    private val radius = 20f * density
    private val divider = density.coerceAtLeast(1f)
    private val card = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(
            view,
            com.google.android.material.R.attr.colorSurfaceContainerLowest,
        )
        style = Paint.Style.FILL
    }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(
            view,
            com.google.android.material.R.attr.colorOutlineVariant,
        )
        style = Paint.Style.FILL
    }
    private val rect = RectF()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        var index = 0
        while (index < parent.childCount) {
            val child = parent.getChildAt(index)
            val position = parent.getChildAdapterPosition(child)
            if (position == RecyclerView.NO_POSITION || isBreak(parent, position)) {
                index++
                continue
            }
            var end = index
            while (end + 1 < parent.childCount) {
                val next = parent.getChildAt(end + 1)
                val nextPosition = parent.getChildAdapterPosition(next)
                if (nextPosition == RecyclerView.NO_POSITION || isBreak(parent, nextPosition)) break
                end++
            }
            val first = parent.getChildAt(index)
            val last = parent.getChildAt(end)
            val firstPosition = parent.getChildAdapterPosition(first)
            val lastPosition = parent.getChildAdapterPosition(last)
            val roundTop = firstPosition <= 0 || isBreak(parent, firstPosition - 1)
            val roundBottom = lastPosition >= adapter.itemCount - 1 ||
                isBreak(parent, lastPosition + 1)
            rect.set(
                first.left.toFloat(),
                first.top.toFloat(),
                first.right.toFloat(),
                last.bottom.toFloat(),
            )
            drawCard(c, roundTop, roundBottom)
            index = end + 1
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var index = 0
        while (index < parent.childCount) {
            val position = parent.getChildAdapterPosition(parent.getChildAt(index))
            if (position == RecyclerView.NO_POSITION || isBreak(parent, position)) {
                index++
                continue
            }
            var end = index
            while (end + 1 < parent.childCount) {
                val nextPosition = parent.getChildAdapterPosition(parent.getChildAt(end + 1))
                if (nextPosition == RecyclerView.NO_POSITION || isBreak(parent, nextPosition)) break
                end++
            }
            val left = parent.getChildAt(index).left.toFloat()
            val right = parent.getChildAt(index).right.toFloat()
            for (row in index until end) {
                val bottom = parent.getChildAt(row).bottom.toFloat()
                c.drawRect(left, bottom, right, bottom + divider, line)
            }
            index = end + 1
        }
    }

    private fun drawCard(canvas: Canvas, roundTop: Boolean, roundBottom: Boolean) {
        val top = if (roundTop) radius else 0f
        val bottom = if (roundBottom) radius else 0f
        val radii = floatArrayOf(top, top, top, top, bottom, bottom, bottom, bottom)
        val path = android.graphics.Path().apply {
            addRoundRect(rect, radii, android.graphics.Path.Direction.CW)
        }
        canvas.drawPath(path, card)
    }
}
