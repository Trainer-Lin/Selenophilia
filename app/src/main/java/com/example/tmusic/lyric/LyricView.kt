package com.example.tmusic.lyric

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.StaticLayout
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import com.example.tmusic.lyric.data.Lyric
import kotlin.math.max
import kotlin.math.min

class LyricView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var lyric: Lyric = Lyric.EMPTY
    private var currentLineIndex = 0

    private val normalPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#5C605A")
        textSize = 48f // Default size
    }
    private val highlightPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#2F342E")
        textSize = 60f // Highlight size
        isFakeBoldText = true
    }
    private val timelinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        color = Color.parseColor("#888888")
        textSize = 32f
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#888888")
        strokeWidth = 2f
    }

    private var lineHeight = 120f
    private var maxScrollY = 0f
    private var isUserScrolling = false
    private var scrollOffsetY = 0f

    private val scroller = Scroller(context)
    private var scrollAnimator: ValueAnimator? = null
    
    private val userScrollTimeoutRunnable = Runnable { 
        isUserScrolling = false
        scrollToCurrentLine() 
        invalidate()
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            scroller.forceFinished(true)
            scrollAnimator?.cancel()
            removeCallbacks(userScrollTimeoutRunnable)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean {
            isUserScrolling = true
            scrollOffsetY += distanceY
            scrollOffsetY = max(0f, min(scrollOffsetY, maxScrollY))
            invalidate()
            return true
        }

        override fun onFling(
            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            isUserScrolling = true
            scroller.fling(
                0, scrollOffsetY.toInt(),
                0, -velocityY.toInt(),
                0, 0,
                0, maxScrollY.toInt()
            )
            postInvalidateOnAnimation()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (isUserScrolling) {
                val centerLineIndex = getCenterLineIndex()
                if (centerLineIndex in lyric.lines.indices) {
                    onSeekListener?.invoke(lyric.lines[centerLineIndex].startTimeMs)
                    isUserScrolling = false
                    scrollToCurrentLine()
                }
            } else {
                performClick()
            }
            return true
        }
    }
    
    private val gestureDetector = GestureDetector(context, gestureListener)

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newSize = normalPaint.textSize * scaleFactor
            if (newSize in 30f..120f) {
                setFontSize(newSize)
            }
            return true
        }
    })

    var onSeekListener: ((Long) -> Unit)? = null

    init {
        updateLineHeight()
    }

    fun setLyric(newLyric: Lyric) {
        this.lyric = newLyric
        this.currentLineIndex = 0
        this.scrollOffsetY = 0f
        this.isUserScrolling = false
        scroller.forceFinished(true)
        scrollAnimator?.cancel()
        calculateMaxScrollY()
        invalidate()
    }

    fun updateTime(timeMs: Long) {
        if (lyric.lines.isEmpty()) return

        val newIndex = lyric.getLineIndexAt(timeMs)
        if (newIndex != currentLineIndex && newIndex != -1) {
            currentLineIndex = newIndex
            if (!isUserScrolling) {
                scrollToCurrentLine()
            }
            invalidate()
        }
    }

    fun setFontSize(size: Float) {
        normalPaint.textSize = size
        highlightPaint.textSize = size * 1.16f
        updateLineHeight()
        calculateMaxScrollY()
        if (!isUserScrolling) {
            scrollToCurrentLine()
        }
        invalidate()
    }

    fun setNightMode(isNight: Boolean) {
        if (isNight) {
            normalPaint.color = Color.parseColor("#A0A0A0")
            highlightPaint.color = Color.parseColor("#FFFFFF")
            timelinePaint.color = Color.parseColor("#888888")
            linePaint.color = Color.parseColor("#555555")
        } else {
            normalPaint.color = Color.parseColor("#5C605A")
            highlightPaint.color = Color.parseColor("#2F342E")
            timelinePaint.color = Color.parseColor("#888888")
            linePaint.color = Color.parseColor("#CCCCCC")
        }
        invalidate()
    }

    private fun updateLineHeight() {
        val fm = normalPaint.fontMetrics
        // Adjust spacing so that roughly 5 lines fit on the screen
        lineHeight = fm.descent - fm.ascent + 80f
    }

    private fun calculateMaxScrollY() {
        maxScrollY = if (lyric.lines.isEmpty()) 0f else max(0f, (lyric.lines.size - 1) * lineHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateMaxScrollY()
        if (!isUserScrolling) {
            scrollOffsetY = currentLineIndex * lineHeight
            invalidate()
        }
    }

    private fun scrollToCurrentLine() {
        if (lyric.lines.isEmpty()) return
        val targetY = max(0f, min(currentLineIndex * lineHeight, maxScrollY))
        
        scrollAnimator?.cancel()
        scrollAnimator = ValueAnimator.ofFloat(scrollOffsetY, targetY).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                scrollOffsetY = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun getCenterLineIndex(): Int {
        if (lineHeight == 0f) return 0
        return Math.round(scrollOffsetY / lineHeight).coerceIn(0, max(0, lyric.lines.size - 1))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (lyric.lines.isEmpty()) return super.onTouchEvent(event)

        scaleDetector.onTouchEvent(event)
        if (!scaleDetector.isInProgress) {
            gestureDetector.onTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isUserScrolling) {
                    postDelayed(userScrollTimeoutRunnable, 3000)
                }
            }
        }

        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollOffsetY = scroller.currY.toFloat()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (lyric.lines.isEmpty()) {
            val fm = normalPaint.fontMetrics
            val y = height / 2f - (fm.descent + fm.ascent) / 2f
            canvas.drawText("暂无歌词", width / 2f, y, normalPaint)
            return
        }

        val centerY = height / 2f
        val startLine = max(0, ((scrollOffsetY - centerY) / lineHeight).toInt() - 2)
        val endLine = min(lyric.lines.size - 1, ((scrollOffsetY + centerY) / lineHeight).toInt() + 2)

        val centerIndex = getCenterLineIndex()

        for (i in startLine..endLine) {
            val y = centerY + (i * lineHeight) - scrollOffsetY
            
            // Check visibility
            if (y < -lineHeight || y > height + lineHeight) continue

            // If user is scrolling, highlight the center line, otherwise highlight current playing line
            val isHighlighted = if (isUserScrolling) i == centerIndex else i == currentLineIndex
            val paint = if (isHighlighted) highlightPaint else normalPaint
            
            val fm = paint.fontMetrics
            val textY = y - (fm.descent + fm.ascent) / 2f

            canvas.drawText(lyric.lines[i].content, width / 2f, textY, paint)
        }

        if (isUserScrolling) {
            if (centerIndex in lyric.lines.indices) {
                val timeMs = lyric.lines[centerIndex].startTimeMs
                val timeStr = formatTime(timeMs)
                
                val fm = timelinePaint.fontMetrics
                val textY = centerY - (fm.descent + fm.ascent) / 2f
                
                canvas.drawText(timeStr, 40f, textY, timelinePaint)
                canvas.drawLine(180f, centerY, width - 40f, centerY, linePaint)
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
