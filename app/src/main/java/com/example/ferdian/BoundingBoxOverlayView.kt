package com.example.ferdian

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class BoundingBoxOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var onDrawCallback: ((Canvas) -> Unit)? = null

    fun setOnDraw(callback: (Canvas) -> Unit) {
        onDrawCallback = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onDrawCallback?.invoke(canvas)
    }
}