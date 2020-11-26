package net.atrin.persian_week_view_library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.*
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.jakewharton.threetenabp.AndroidThreeTen
import net.atrin.persian_week_view_library.WeekViewUtil.daysBetween
import net.atrin.persian_week_view_library.WeekViewUtil.getPassedMinutesInDay
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class WeekView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(
    context,
    attrs,
    defStyleAttr
) {
    private val now: LocalDateTime
    private val mContext: Context
    private var mHomeDay: DayOfWeek? = null
    private var mMinDay: DayOfWeek? = null
    private var mMaxDay: DayOfWeek? = null
    private var mTimeTextPaint: Paint? = null
    private var mTimeTextWidth = 0f
    private var mTimeTextHeight = 0f
    private var mHeaderTextPaint: Paint? = null
    private var mHeaderTextHeight = 0f
    private var mHeaderHeight = 0f
    private var mGestureDetector: GestureDetector? = null
    private var mScroller: OverScroller? = null
    private val mCurrentOrigin = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private var mHeaderBackgroundPaint: Paint? = null
    private var mWidthPerDay = 0f
    private var mDayBackgroundPaint: Paint? = null
    private var mHourSeparatorPaint: Paint? = null
    private var mHeaderMarginBottom = 0f
    private var mTodayBackgroundPaint: Paint? = null
    private var mFutureBackgroundPaint: Paint? = null
    private var mPastBackgroundPaint: Paint? = null
    private var mFutureWeekendBackgroundPaint: Paint? = null
    private var mPastWeekendBackgroundPaint: Paint? = null
    private var mNowLinePaint: Paint? = null
    private var mTodayHeaderTextPaint: Paint? = null
    private var mEventBackgroundPaint: Paint? = null
    private var mHeaderColumnWidth = 0f
    private var mEventRects: MutableList<EventRect>? = null
    private var mEvents: MutableList<WeekViewEvent>? = null
    private var mEventTextPaint: TextPaint? = null
    private var mHeaderColumnBackgroundPaint: Paint? = null
    private var mFetchedPeriod = -1 // the middle period the calendar has fetched.
    private var mRefreshEvents = false
    private var mCurrentFlingDirection = Direction.NONE
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mIsZooming = false
    private var mFirstVisibleDay: DayOfWeek? = null
    private var mLastVisibleDay: DayOfWeek? = null
    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0
    private var mNewEventRect: EventRect? = null
    var textColorPicker: TextColorPicker? = null

    // Attributes and their default values.
    private var mHourHeight = 50
    private var mNewHourHeight = -1
    private var mMinHourHeight = 0 //no minimum specified (will be dynamic, based on screen)
    private var mEffectiveMinHourHeight = mMinHourHeight //compensates for the fact that you can't keep zooming out.
    private var mMaxHourHeight = 250
    private var mColumnGap = 10
    private var mFirstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    private var mTextSize = 12
    private var mHeaderColumnPadding = 10
    private var mHeaderColumnTextColor: Int = Color.BLACK
    private var mNumberOfVisibleDays = 3
    private var mHeaderRowPadding = 10
    private var mHeaderRowBackgroundColor: Int = Color.WHITE
    private var mDayBackgroundColor: Int = Color.rgb(245, 245, 245)
    private var mPastBackgroundColor: Int = Color.rgb(227, 227, 227)
    private var mFutureBackgroundColor: Int = Color.rgb(245, 245, 245)
    private var mPastWeekendBackgroundColor = 0
    private var mFutureWeekendBackgroundColor = 0
    private var mNowLineColor: Int = Color.rgb(102, 102, 102)
    private var mNowLineThickness = 5
    private var mHourSeparatorColor: Int = Color.rgb(230, 230, 230)
    private var mTodayBackgroundColor: Int = Color.rgb(239, 247, 254)
    private var mHourSeparatorHeight = 2
    private var mTodayHeaderTextColor: Int = Color.rgb(39, 137, 228)
    private var mEventTextSize = 12
    private var mEventTextColor: Int = Color.BLACK
    private var mEventPadding = 8
    private var mHeaderColumnBackgroundColor: Int = Color.WHITE
    private var mDefaultEventColor = 0
    private var mNewEventColor = 0
    private var mNewEventIdentifier: String? = "-100"
    private var mNewEventIconDrawable: Drawable? = null
    private var mNewEventLengthInMinutes = 60
    private var mNewEventTimeResolutionInMinutes = 15
    private var mShowFirstDayOfWeekFirst = false
    private var mIsFirstDraw = true
    private var mAreDimensionsInvalid = true
    private var mOverlappingEventGap = 0
    private var mEventMarginVertical = 0
    private var mXScrollingSpeed = 1f
    private var mScrollToDay: DayOfWeek? = null
    private var mScrollToHour = -1.0
    private var mEventCornerRadius = 0
    private var mShowDistinctWeekendColor = false
    private var mShowNowLine = true
    private var mShowDistinctPastFutureColor = false
    private var mHorizontalFlingEnabled = true
    private var mVerticalFlingEnabled = true
    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    /**
     * Set the height of AllDay-events.
     *
     * @param height the new height of AllDay-events
     */
    var allDayEventHeight = 100
    private var mZoomFocusPoint = 0f
    private var mZoomFocusPointEnabled = true
    private var mScrollDuration = 250
    private var mTimeColumnResolution = 60
    private var mTypeface = Typeface.DEFAULT_BOLD
    private var mMinTime = 0
    private var mMaxTime = 24
    private var mAutoLimitTime = false
    private var mEnableDropListener = false
    private var mMinOverlappingMinutes = 0

    // Listeners.
    private var mEventClickListener: EventClickListener? = null
    private var mEventLongPressListener: EventLongPressListener? = null
    private var mWeekViewLoader: WeekViewLoader? = null
    private var mEmptyViewClickListener: EmptyViewClickListener? = null
    private var mEmptyViewLongPressListener: EmptyViewLongPressListener? = null
    private var mDayTimeInterpreter: DayTimeInterpreter? = null
    var addEventClickListener: AddEventClickListener? = null
    private val mGestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            goToNearestOrigin()
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (mIsZooming) {
                return true
            }
            if (mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled ||
                mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled) {
                return true
            }
            mScroller!!.forceFinished(true)
            mCurrentFlingDirection = mCurrentScrollDirection
            when (mCurrentFlingDirection) {
                Direction.LEFT, Direction.RIGHT -> mScroller!!.fling(
                    mCurrentOrigin.x.toInt(),
                    mCurrentOrigin.y.toInt(),
                    (velocityX *
                            mXScrollingSpeed).toInt(),
                    0,
                    getXMinLimit().toInt(),
                    getXMaxLimit().toInt(),
                    getYMinLimit().toInt(),
                    getYMaxLimit().toInt()
                )
                Direction.VERTICAL -> mScroller!!.fling(
                    mCurrentOrigin.x.toInt(),
                    mCurrentOrigin.y.toInt(),
                    0,
                    velocityY.toInt(),
                    getXMinLimit().toInt(),
                    getXMaxLimit().toInt(),
                    getYMinLimit().toInt(),
                    getYMaxLimit().toInt()
                )
                else -> {
                }
            }
            postInvalidateOnAnimation()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            if (mEventLongPressListener != null && mEventRects != null) {
                val reversedEventRects: List<EventRect> = mEventRects as MutableList<EventRect>
                Collections.reverse(reversedEventRects)
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        mEventLongPressListener!!.onEventLongPress(event.originalEvent, event.rectF)
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        return
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
            if (mEmptyViewLongPressListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom) {
                val selectedTime: DayTime? = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    mEmptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime)
                }
            }
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Check if view is zoomed.
            if (mIsZooming) {
                return true
            }
            when (mCurrentScrollDirection) {
                Direction.NONE -> {

                    // Allow scrolling only in one direction.
                    mCurrentScrollDirection = if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            Direction.LEFT
                        } else {
                            Direction.RIGHT
                        }
                    } else {
                        Direction.VERTICAL
                    }
                }
                Direction.LEFT -> {

                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.RIGHT
                    }
                }
                Direction.RIGHT -> {

                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.LEFT
                    }
                }
                else -> {
                }
            }
            when (mCurrentScrollDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    val minX = getXMinLimit()
                    val maxX = getXMaxLimit()
                    if (mCurrentOrigin.x - distanceX * mXScrollingSpeed > maxX) {
                        mCurrentOrigin.x = maxX
                    } else if (mCurrentOrigin.x - distanceX * mXScrollingSpeed < minX) {
                        mCurrentOrigin.x = minX
                    } else {
                        mCurrentOrigin.x -= distanceX * mXScrollingSpeed
                    }
                    postInvalidateOnAnimation()
                }
                Direction.VERTICAL -> {
                    val minY = getYMinLimit()
                    val maxY = getYMaxLimit()
                    if (mCurrentOrigin.y - distanceY > maxY) {
                        mCurrentOrigin.y = maxY
                    } else if (mCurrentOrigin.y - distanceY < minY) {
                        mCurrentOrigin.y = minY
                    } else {
                        mCurrentOrigin.y -= distanceY
                    }
                    postInvalidateOnAnimation()
                }
                else -> {
                }
            }
            return true
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

            // If the tap was on an event then trigger the callback.
            if (mEventRects != null && mEventClickListener != null) {
                val reversedEventRects: List<EventRect> = mEventRects as MutableList<EventRect>
                Collections.reverse(reversedEventRects)
                for (eventRect in reversedEventRects) {
                    if (mNewEventIdentifier != eventRect.event.identifier && eventRect.rectF != null && e
                            .x > eventRect.rectF!!.left && e.x < eventRect.rectF!!.right && e.y >
                        eventRect.rectF!!.top && e.y < eventRect.rectF!!.bottom) {
                        mEventClickListener!!.onEventClick(eventRect.originalEvent, eventRect.rectF)
                        playSoundEffect(SoundEffectConstants.CLICK)
                        return super.onSingleTapConfirmed(e)
                    }
                }
            }
            val xOffset = getXStartPixel()
            val x = e.x - xOffset
            val y = e.y - mCurrentOrigin.y
            // If the tap was on add new Event space, then trigger the callback
            if (addEventClickListener != null && mNewEventRect != null && mNewEventRect!!.rectF != null &&
                mNewEventRect!!.rectF!!.contains(x, y)) {
                addEventClickListener!!.onAddEventClicked(
                    mNewEventRect!!.event.startTime, mNewEventRect!!.event
                        .endTime
                )
                return super.onSingleTapConfirmed(e)
            }

            // If the tap was on an empty space, then trigger the callback.
            if ((mEmptyViewClickListener != null || addEventClickListener != null) && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom) {
                val selectedTime: DayTime? = getTimeFromPoint(e.x, e.y)
                if (selectedTime != null) {
                    val tempEvents: MutableList<WeekViewEvent> = ArrayList(mEvents)
                    if (mNewEventRect != null) {
                        tempEvents.remove(mNewEventRect!!.event)
                        mNewEventRect = null
                    }
                    playSoundEffect(SoundEffectConstants.CLICK)
                    if (mEmptyViewClickListener != null) {
                        mEmptyViewClickListener!!.onEmptyViewClicked(DayTime(selectedTime))
                    }
                    if (addEventClickListener != null) {
                        //round selectedTime to resolution
                        selectedTime.subtractMinutes(mNewEventLengthInMinutes / 2)
                        //Fix selected time if before the minimum hour
                        if (selectedTime.minute < mMinTime) {
                            selectedTime.setTime(mMinTime, 0)
                        }
                        val unroundedMinutes: Int = selectedTime.minute
                        val mod = unroundedMinutes % mNewEventTimeResolutionInMinutes
                        selectedTime.addMinutes(if (mod < Math.ceil(mNewEventTimeResolutionInMinutes / 2.toDouble())) -mod else mNewEventTimeResolutionInMinutes - mod)
                        val endTime = DayTime(selectedTime)

                        //Minus one to ensure it is the same day and not midnight (next day)
                        val maxMinutes: Int = (mMaxTime - selectedTime.hour) * 60 - selectedTime.minute - 1
                        endTime.addMinutes(Math.min(maxMinutes, mNewEventLengthInMinutes))
                        //If clicked at end of the day, fix selected startTime
                        if (maxMinutes < mNewEventLengthInMinutes) {
                            selectedTime.addMinutes(maxMinutes - mNewEventLengthInMinutes)
                        }
                        val newEvent = WeekViewEvent(
                            mNewEventIdentifier, "", null, selectedTime,
                            endTime
                        )
                        val top: Float = mHourHeight * getPassedMinutesInDay(selectedTime) / 60 + getEventsTop()
                        val bottom: Float = mHourHeight * getPassedMinutesInDay(endTime) / 60 + getEventsTop()

                        // Calculate left and right.
                        val left: Float = mWidthPerDay * WeekViewUtil.daysBetween(
                            getFirstVisibleDay()!!, selectedTime
                                .getDay()!!
                        )
                        val right = left + mWidthPerDay

                        // Add the new event if its bounds are valid
                        if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > 0) {
                            val dayRectF = RectF(left, top, right, bottom - mCurrentOrigin.y)
                            newEvent.color = mNewEventColor
                            mNewEventRect = EventRect(newEvent, newEvent, dayRectF)
                            tempEvents.add(newEvent)
                            clearEvents()
                            cacheAndSortEvents(tempEvents)
                            computePositionOfEvents(mEventRects)
                            invalidate()
                        }
                    }
                }
            }
            return super.onSingleTapConfirmed(e)
        }
    }
    private var mDropListener: DropListener? = null

    constructor(context: Context) : this(context, null) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    /**
     * Cache and sort events.
     *
     * @param events The events to be cached and sorted.
     */
    private fun cacheAndSortEvents(events: List<WeekViewEvent>) {
        for (event in events) {
            cacheEvent(event)
        }
        sortEventRects(mEventRects)
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent) {
        if (event.startTime!!.compareTo(event.endTime) >= 0) {
            return
        }
        val splitedEvents: List<WeekViewEvent> = event.splitWeekViewEvents()
        for (splitedEvent in splitedEvents) {
            mEventRects!!.add(EventRect(splitedEvent, event, null))
        }
        mEvents!!.add(event)
    }

    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (dayNumber in 0 until getRealNumberOfVisibleDays()) {
                val day: DayOfWeek = getFirstVisibleDay()!!.plus(dayNumber.toLong())
                for (i in mEventRects!!.indices) {
                    if (mEventRects!![i].event.startTime!!.getDay() === day && mEventRects!![i].event.isAllDay) {
                        containsAllDayEvent = true
                        break
                    }
                }
                if (containsAllDayEvent) {
                    break
                }
            }
        }
        mHeaderHeight = if (containsAllDayEvent) {
            mHeaderTextHeight + (allDayEventHeight + mHeaderMarginBottom)
        } else {
            mHeaderTextHeight
        }
    }

    private fun clearEvents() {
        mEventRects!!.clear()
        mEvents!!.clear()
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private fun computePositionOfEvents(eventRects: List<EventRect>?) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups: MutableList<MutableList<EventRect>> = ArrayList()
        for (eventRect in eventRects!!) {
            var isPlaced = false
            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.isAllDay ===
                        eventRect.event.isAllDay) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }
            if (!isPlaced) {
                val newGroup: MutableList<EventRect> = ArrayList()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }
        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param day The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawAllDayEvents(day: DayOfWeek, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                if (mEventRects!![i].event.startTime!!.getDay() === day && mEventRects!![i].event.isAllDay) {

                    // Calculate top.
                    val top = mHeaderRowPadding * 2 + mHeaderMarginBottom + +mTimeTextHeight / 2 +
                            mEventMarginVertical

                    // Calculate bottom.
                    val bottom = top + mEventRects!![i].bottom

                    // Calculate left and right.
                    var left = startFromPixel + mEventRects!![i].left * mWidthPerDay
                    if (left < startFromPixel) {
                        left += mOverlappingEventGap.toFloat()
                    }
                    var right = left + mEventRects!![i].width * mWidthPerDay
                    if (right < startFromPixel + mWidthPerDay) {
                        right -= mOverlappingEventGap.toFloat()
                    }

                    // Draw the event and the event name on top of it.
                    if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > 0) {
                        mEventRects!![i].rectF = RectF(left, top, right, bottom)
                        mEventBackgroundPaint!!.setColor(if (mEventRects!![i].event.color == 0) mDefaultEventColor else mEventRects!![i].event.color)
                        mEventBackgroundPaint!!.setShader(mEventRects!![i].event.shader)
                        canvas.drawRoundRect(
                            mEventRects!![i].rectF!!,
                            mEventCornerRadius.toFloat(), mEventCornerRadius.toFloat(),
                            mEventBackgroundPaint!!
                        )
                        drawEventTitle(
                            mEventRects!![i].event,
                            mEventRects!![i].rectF,
                            canvas,
                            top,
                            left
                        )
                    } else {
                        mEventRects!![i].rectF = null
                    }
                }
            }
        }
    }

    /**
     * Draw the text on top of the rectangle in the empty event.
     */
    private fun drawEmptyImage(
        rect: RectF?,
        canvas: Canvas,
        originalTop: Float,
        originalLeft: Float
    ) {
        val size = Math.max(
            1,
            Math.floor(Math.min(0.8 * rect!!.height(), 0.8 * rect.width())).toInt()
        )
        if (mNewEventIconDrawable == null) {
            mNewEventIconDrawable = getResources().getDrawable(R.drawable.ic_add, null)
        }
        var icon = (mNewEventIconDrawable as BitmapDrawable).bitmap
        icon = Bitmap.createScaledBitmap(icon, size, size, false)
        canvas.drawBitmap(
            icon, originalLeft + (rect.width() - icon.width) / 2, originalTop + (rect.height() -
                    icon.height) / 2, Paint()
        )
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event The event of which the title (and location) should be drawn.
     * @param rect The rectangle on which the text is to be drawn.
     * @param canvas The canvas to draw upon.
     * @param originalTop The original top position of the rectangle. The rectangle may have some of its portion
     * outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion
     * outside of the visible area.
     */
    private fun drawEventTitle(
        event: WeekViewEvent,
        rect: RectF?,
        canvas: Canvas,
        originalTop: Float,
        originalLeft: Float
    ) {
        if (rect!!.right - rect.left - mEventPadding * 2 < 0) {
            return
        }
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) {
            return
        }

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        if (!TextUtils.isEmpty(event.name)) {
            bob.append(event.name)
            bob.setSpan(StyleSpan(Typeface.BOLD), 0, bob.length, 0)
        }
        // Prepare the location of the event.
        if (!TextUtils.isEmpty(event.location)) {
            if (bob.length > 0) {
                bob.append(' ')
            }
            bob.append(event.location)
        }
        val availableHeight = (rect.bottom - originalTop - mEventPadding * 2).toInt()
        val availableWidth = (rect.right - originalLeft - mEventPadding * 2).toInt()

        // Get text color if necessary
        if (textColorPicker != null) {
            mEventTextPaint!!.color = textColorPicker!!.getTextColor(event)
        }
        // Get text dimensions.
        var textLayout = StaticLayout(
            bob,
            mEventTextPaint,
            availableWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1.0f,
            0.0f,
            false
        )
        if (textLayout.lineCount > 0) {
            val lineHeight = textLayout.height / textLayout.lineCount
            if (availableHeight >= lineHeight) {
                // Calculate available number of line counts.
                var availableLineCount = availableHeight / lineHeight
                do {
                    // Ellipsize text to fit into event rect.
                    if (mNewEventIdentifier != event.identifier) {
                        textLayout = StaticLayout(
                            TextUtils.ellipsize(
                                bob, mEventTextPaint, availableLineCount *
                                        availableWidth.toFloat(), TextUtils.TruncateAt.END
                            ),
                            mEventTextPaint,
                            (rect.right -
                                    originalLeft
                                    - mEventPadding * 2).toInt(),
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0.0f,
                            false
                        )
                    }

                    // Reduce line count.
                    availableLineCount--

                    // Repeat until text is short enough.
                } while (textLayout.height > availableHeight)

                // Draw text.
                canvas.save()
                canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding)
                textLayout.draw(canvas)
                canvas.restore()
            }
        }
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param day The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawEvents(day: DayOfWeek?, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            for (i in mEventRects!!.indices) {
                if (mEventRects!![i].event.startTime!!.getDay() === day && !mEventRects!![i].event.isAllDay) {
                    val top = mHourHeight * mEventRects!![i].top / 60 + getEventsTop()
                    val bottom = mHourHeight * mEventRects!![i].bottom / 60 + getEventsTop()

                    // Calculate left and right.
                    var left = startFromPixel + mEventRects!![i].left * mWidthPerDay
                    if (left < startFromPixel) {
                        left += mOverlappingEventGap.toFloat()
                    }
                    var right = left + mEventRects!![i].width * mWidthPerDay
                    if (right < startFromPixel + mWidthPerDay) {
                        right -= mOverlappingEventGap.toFloat()
                    }

                    // Draw the event and the event name on top of it.
                    if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth && bottom > mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                        mEventRects!![i].rectF = RectF(left, top, right, bottom)
                        mEventBackgroundPaint!!.setColor(if (mEventRects!![i].event.color == 0) mDefaultEventColor else mEventRects!![i].event.color)
                        mEventBackgroundPaint!!.setShader(mEventRects!![i].event.shader)
                        canvas.drawRoundRect(
                            mEventRects!![i].rectF!!,
                            mEventCornerRadius.toFloat(), mEventCornerRadius.toFloat(),
                            mEventBackgroundPaint!!
                        )
                        var topToUse = top
                        if (mEventRects!![i].event.startTime!!.time!!.getHour() < mMinTime) {
                            topToUse = mHourHeight * getPassedMinutesInDay(mMinTime, 0) / 60 + getEventsTop()
                        }
                        if (mNewEventIdentifier != mEventRects!![i].event.identifier) {
                            drawEventTitle(
                                mEventRects!![i].event,
                                mEventRects!![i].rectF,
                                canvas,
                                topToUse,
                                left
                            )
                        } else {
                            drawEmptyImage(mEventRects!![i].rectF, canvas, topToUse, left)
                        }
                    } else {
                        mEventRects!![i].rectF = null
                    }
                }
            }
        }
    }

    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2
        mWidthPerDay = getWidth() - mHeaderColumnWidth - mColumnGap * (getRealNumberOfVisibleDays() - 1)
        mWidthPerDay = mWidthPerDay / getRealNumberOfVisibleDays()
        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)
        val today: LocalDateTime = now
        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(
                mMinHourHeight,
                ((getHeight() - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) / (mMaxTime - mMinTime)).toInt()
            )
            mAreDimensionsInvalid = false
            if (mScrollToDay != null) {
                goToDay(mScrollToDay)
            }
            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0) {
                goToHour(mScrollToHour)
            }
            mScrollToDay = null
            mScrollToHour = -1.0
            mAreDimensionsInvalid = false
        }
        if (mIsFirstDraw) {
            mIsFirstDraw = false

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (getRealNumberOfVisibleDays() >= 7 && mHomeDay !== mFirstDayOfWeek && mShowFirstDayOfWeekFirst) {
                val difference: Int = mHomeDay!!.getValue() - mFirstDayOfWeek.getValue()
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference
            }
            setLimitTime(mMinTime, mMaxTime)
        }

        // Calculate the new height due to the zooming.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight) {
                mNewHourHeight = mEffectiveMinHourHeight
            } else if (mNewHourHeight > mMaxHourHeight) {
                mNewHourHeight = mMaxHourHeight
            }
            mHourHeight = mNewHourHeight
            mNewHourHeight = -1
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (mCurrentOrigin.y < height - mHourHeight * (mMaxTime - mMinTime) - mHeaderHeight - (mHeaderRowPadding
                    * 2) - mHeaderMarginBottom - mTimeTextHeight / 2) {
            mCurrentOrigin.y = height - mHourHeight * (mMaxTime - mMinTime) - mHeaderHeight - (mHeaderRowPadding
                    * 2) - mHeaderMarginBottom - mTimeTextHeight / 2
        }

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0f
        }
        val leftDaysWithGaps = getLeftDaysWithGaps()
        // Consider scroll offset.
        val startFromPixel = getXStartPixel()
        var startPixel = startFromPixel

        // Prepare to iterate for each hour to draw the hour lines.
        var lineCount = ((height - mHeaderHeight - mHeaderRowPadding * 2 -
                mHeaderMarginBottom) / mHourHeight).toInt() + 1
        lineCount *= (getRealNumberOfVisibleDays() + 1)
        val hourLines = FloatArray(lineCount * 4)

        // Clear the cache for event rectangles.
        if (mEventRects != null) {
            for (eventRect in mEventRects!!) {
                eventRect.rectF = null
            }
        }

        // Clip to paint events only.
        canvas.save()
        canvas.clipRect(
            mHeaderColumnWidth,
            mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2,
            width.toFloat(),
            height.toFloat()
        )

        // Iterate through each day.
        mFirstVisibleDay = mHomeDay
        mFirstVisibleDay!!.minus(
            Math.round(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)).toLong()
        )
        if (mAutoLimitTime) {
            val days: MutableList<DayOfWeek?> = ArrayList()
            for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays()) {
                val day: DayOfWeek? = mHomeDay
                day!!.plus((dayNumber - 1).toLong())
                days.add(day)
            }
            limitEventTime(days)
        }
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {

            // Check if the day is today.
            var day: DayOfWeek? = mHomeDay
            mLastVisibleDay = day
            day = day!!.plus((dayNumber - 1).toLong())
            mLastVisibleDay!!.plus((dayNumber - 2).toLong())
            val isToday = day === today.dayOfWeek

            // Don't draw days which are outside requested range
            if (!dayIsValid(day)) {
                continue
            }

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mEventRects == null || mRefreshEvents) {
                getMoreEvents(day)
                mRefreshEvents = false
            }

            // Draw background color for each day.
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0) {
                if (mShowDistinctPastFutureColor) {
                    val isWeekend = day === DayOfWeek.SATURDAY || day === DayOfWeek.SUNDAY
                    val pastPaint: Paint = if (isWeekend && mShowDistinctWeekendColor) mPastWeekendBackgroundPaint!! else mPastBackgroundPaint!!
                    val futurePaint: Paint = if (isWeekend && mShowDistinctWeekendColor) mFutureWeekendBackgroundPaint!! else mFutureBackgroundPaint!!
                    val startY = (mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom
                            + mCurrentOrigin.y)
                    if (isToday) {
                        val beforeNow: Float = (now.getHour() - mMinTime + now.getMinute() / 60.0f) * mHourHeight
                        canvas.drawRect(
                            start,
                            startY,
                            startPixel + mWidthPerDay,
                            startY + beforeNow,
                            pastPaint
                        )
                        canvas.drawRect(
                            start,
                            startY + beforeNow,
                            startPixel + mWidthPerDay,
                            height.toFloat(),
                            futurePaint
                        )
                    } else if (day.compareTo(today.getDayOfWeek()) < 0) {
                        canvas.drawRect(
                            start,
                            startY,
                            startPixel + mWidthPerDay,
                            height.toFloat(),
                            pastPaint
                        )
                    } else {
                        canvas.drawRect(
                            start,
                            startY,
                            startPixel + mWidthPerDay,
                            height.toFloat(),
                            futurePaint
                        )
                    }
                } else {
                    canvas.drawRect(
                        start,
                        mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 +
                                mHeaderMarginBottom,
                        startPixel + mWidthPerDay,
                        height.toFloat(),
                        if (isToday) mTodayBackgroundPaint!! else mDayBackgroundPaint!!
                    )
                }
            }

            // Prepare the separator lines for hours.
            var i = 0
            for (hourNumber in mMinTime until mMaxTime) {
                val top = mHeaderHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + mHourHeight * (hourNumber -
                        mMinTime) + mTimeTextHeight / 2 + mHeaderMarginBottom
                if (top > mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom -
                    mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start
                    hourLines[i * 4 + 1] = top
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay
                    hourLines[i * 4 + 3] = top
                    i++
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, mHourSeparatorPaint!!)

            // Draw the events.
            drawEvents(day, startPixel, canvas)

            // Draw the line at the current time.
            if (mShowNowLine && isToday) {
                val startY = mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom +
                        mCurrentOrigin.y
                val beforeNow: Float = (now.getHour() - mMinTime + now.getMinute() / 60.0f) * mHourHeight
                val top = startY + beforeNow
                canvas.drawLine(start, top, startPixel + mWidthPerDay, top, mNowLinePaint!!)
            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap
        }
        canvas.restore() // Restore previous clip

        // Hide everything in the first cell (top left corner).
        canvas.save()
        canvas.clipRect(
            0f,
            0f,
            mTimeTextWidth + mHeaderColumnPadding * 2,
            mHeaderHeight + mHeaderRowPadding * 2
        )
        canvas.drawRect(
            0f,
            0f,
            mTimeTextWidth + mHeaderColumnPadding * 2,
            mHeaderHeight + mHeaderRowPadding * 2,
            mHeaderBackgroundPaint!!
        )
        canvas.restore() // Restore previous clip

        // Clip to paint header row only.
        canvas.save()
        canvas.clipRect(
            mHeaderColumnWidth.toFloat(),
            0f,
            width.toFloat(),
            mHeaderHeight + mHeaderRowPadding * 2
        )

        // Draw the header background.
        canvas.drawRect(
            0f, 0f,
            width.toFloat(), mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint!!
        )

        // Draw the header row texts.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {
            // Check if the day is today.
            val day: DayOfWeek = mHomeDay!!.plus((dayNumber - 1).toLong())
            val isToday = day === today.dayOfWeek

            // Don't draw days which are outside requested range
            if (!dayIsValid(day)) {
                continue
            }

            // Draw the day labels.
            var dayLabel = dayTimeInterpreter!!.interpretDay(day.value)
                ?: throw IllegalStateException("A DayTimeInterpreter must not return null day")
            dayLabel= when(dayLabel){
                "Saturday"->"شنبه"
                "Sunday"->"یکشنبه"
                "Monday"->"دوشنبه"
                "Tuesday"->"سه شنبه"
                "Wednesday"->"چهارشنبه"
                "Thursday"->"پنجشنبه"
                "Friday"->"جمعه"
                else-> dayLabel
            }

            dayLabel= when(dayLabel){
                "Sat"->"شنبه"
                "Sun"->"یکشنبه"
                "Mon"->"دوشنبه"
                "Tue"->"سه شنبه"
                "Wed"->"چهارشنبه"
                "Thu"->"پنجشنبه"
                "Fri"->"جمعه"
                else-> dayLabel
            }

            canvas.drawText(
                dayLabel,
                startPixel + mWidthPerDay / 2,
                mHeaderTextHeight + mHeaderRowPadding,
                if (isToday) mTodayHeaderTextPaint!! else mHeaderTextPaint!!
            )
            drawAllDayEvents(day, startPixel, canvas)
            startPixel += mWidthPerDay + mColumnGap
        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(
            0f, mHeaderHeight + mHeaderRowPadding * 2, mHeaderColumnWidth,
            height.toFloat(),
            mHeaderColumnBackgroundPaint!!
        )
        canvas.restore() // Restore previous clip from #drawHeaderRowAndEvents

        // Clip to paint in left column only.
        canvas.save()
        canvas.clipRect(
            0f,
            mHeaderHeight + mHeaderRowPadding * 2,
            mHeaderColumnWidth,
            height.toFloat()
        )
        for (i in 0 until getNumberOfPeriods()) {
            // If we are showing half hours (eg. 5:30am), space the times out by half the hour height
            // and need to provide 30 minutes on each odd period, otherwise, minutes is always 0.
            var timeSpacing: Float
            var minutes: Int
            var hour: Int
            val timesPerHour = 60.0.toFloat() / mTimeColumnResolution
            timeSpacing = mHourHeight / timesPerHour
            hour = mMinTime + i / timesPerHour.toInt()
            minutes = i % timesPerHour.toInt() * (60 / timesPerHour.toInt())


            // Calculate the top of the rectangle where the time text will go
            val top = mHeaderHeight + mHeaderRowPadding * 2 + mCurrentOrigin.y + timeSpacing * i +
                    mHeaderMarginBottom

            // Get the time to be displayed, as a String.
            var time = dayTimeInterpreter!!.interpretTime(hour, minutes)
                ?: throw IllegalStateException("A DayTimeInterpreter must not return null time")
            time= when(time){
                "12AM"->"00"
                "1AM"->"01"
                "2AM"->"02"
                "3AM"->"03"
                "4AM"->"04"
                "5AM"->"05"
                "6AM"->"06"
                "7AM"->"07"
                "8AM"->"08"
                "9AM"->"09"
                "10AM"->"10"
                "11AM"->"11"
                "12PM"->"12"
                "1PM"->"13"
                "2PM"->"14"
                "3PM"->"15"
                "4PM"->"16"
                "5PM"->"17"
                "6PM"->"18"
                "7PM"->"19"
                "8PM"->"20"
                "9PM"->"21"
                "10PM"->"22"
                else->"23"
            }
            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the
            // point at the bottom-right corner.
            if (top < getHeight()) {
                canvas.drawText(
                    time,
                    mTimeTextWidth + mHeaderColumnPadding,
                    top + mTimeTextHeight,
                    mTimeTextPaint!!
                )
            }
        }
        canvas.restore() // Last restore
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
        // Expand the events to maximum possible width.
        val columns: MutableList<MutableList<EventRect>> = ArrayList()
        columns.add(ArrayList())
        for (eventRect in collisionGroup) {
            var isPlaced = false
            for (column in columns) {
                if (column.size == 0) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect.event, column[column.size - 1].event)) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn: MutableList<EventRect> = ArrayList()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event.isAllDay) {
                        eventRect.top = getPassedMinutesInDay(eventRect.event.startTime!!).toFloat()
                        eventRect.bottom = getPassedMinutesInDay(eventRect.event.endTime!!).toFloat()
                    } else {
                        eventRect.top = 0f
                        eventRect.bottom = allDayEventHeight.toFloat()
                    }
                    mEventRects!!.add(eventRect)
                }
                j++
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean {
        return mScroller!!.currVelocity <= mMinimumFlingVelocity
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private fun getMoreEvents(day: DayOfWeek?) {

        // Get more events if the month is changed.
        if (mEventRects == null) {
            mEventRects = ArrayList()
        }
        if (mEvents == null) {
            mEvents = ArrayList()
        }
        check(!(mWeekViewLoader == null && !isInEditMode())) { "You must provide a WeekViewLoader" }

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            clearEvents()
            mFetchedPeriod = -1
        }
        if (mWeekViewLoader != null) {
            if (!isInEditMode() && (mFetchedPeriod < 0 || mRefreshEvents)) {
                val newEvents = mWeekViewLoader!!.onWeekViewLoad()

                // Clear events.
                clearEvents()
                cacheAndSortEvents(newEvents)
                calculateHeaderHeight()
            }
        }

        // Prepare to calculate positions of each events.
        var tempEvents: ArrayList<EventRect> = mEventRects as ArrayList<EventRect>
        mEventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size > 0) {
            val eventRects: ArrayList<EventRect> = ArrayList(tempEvents.size)

            // Get first event for a day.
            val eventRect1: EventRect = tempEvents.removeAt(0)
            eventRects.add(eventRect1)
            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (eventRect1.event.startTime!!.getDay() === eventRect2.event.startTime!!.getDay()) {
                    tempEvents.removeAt(i)
                    eventRects.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventRects)
        }
    }

    /**
     * Get the time and day where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and day at the clicked position.
     */
    private fun getTimeFromPoint(x: Float, y: Float): DayTime? {
        val leftDaysWithGaps = getLeftDaysWithGaps()
        var startPixel = getXStartPixel()
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                val day = DayTime()
                day.setDay(mHomeDay!!.plus((dayNumber - 1).toLong()))
                val pixelsFromZero = (y - mCurrentOrigin.y - mHeaderHeight
                        - mHeaderRowPadding * 2 - mTimeTextHeight / 2 - mHeaderMarginBottom)
                val hour = (pixelsFromZero / mHourHeight).toInt()
                val minute = (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight).toInt()
                day.setTime(hour + mMinTime, minute)
                return day
            }
            startPixel += mWidthPerDay + mColumnGap
        }
        return null
    }

    private fun getXOriginForDay(day: DayOfWeek): Float {
        return -daysBetween(mHomeDay!!, day) * (mWidthPerDay + mColumnGap)
    }

    private fun goToNearestOrigin() {
        var leftDays = mCurrentOrigin.x / (mWidthPerDay + mColumnGap).toDouble()
        leftDays = if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            Math.floor(leftDays)
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            Math.ceil(leftDays)
        } else {
            // snap to nearest day
            Math.round(leftDays).toDouble()
        }
        val nearestOrigin = (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap)).toInt()
        val mayScrollHorizontal = (mCurrentOrigin.x - nearestOrigin < getXMaxLimit()
                && mCurrentOrigin.x - nearestOrigin > getXMinLimit())
        if (mayScrollHorizontal) {
            mScroller!!.startScroll(
                mCurrentOrigin.x.toInt(),
                mCurrentOrigin.y.toInt(),
                -nearestOrigin,
                0
            )
            postInvalidateOnAnimation()
        }
        if (nearestOrigin != 0 && mayScrollHorizontal) {
            // Stop current animation.
            mScroller!!.forceFinished(true)
            // Snap to day.
            mScroller!!.startScroll(
                mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), -nearestOrigin, 0, (Math.abs(
                    nearestOrigin
                ) / mWidthPerDay * mScrollDuration).toInt()
            )
            postInvalidateOnAnimation()
        }
        // Reset scrolling and fling direction.
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
    }

    private fun init() {
        resetHomeDay()

        // Scrolling initialization.
        mGestureDetector = GestureDetector(mContext, mGestureListener)
        mScroller = OverScroller(mContext, AccelerateDecelerateInterpolator())
        mMinimumFlingVelocity = ViewConfiguration.get(mContext).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop

        // Measure settings for time column.
        mTimeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTimeTextPaint!!.setTextAlign(Paint.Align.RIGHT)
        mTimeTextPaint!!.setTextSize(mTextSize.toFloat())
        mTimeTextPaint!!.setColor(mHeaderColumnTextColor)
        val rect = Rect()
        val exampleTime = if (mTimeColumnResolution % 60 != 0) "00:00 PM" else "00 PM"
        mTimeTextPaint!!.getTextBounds(exampleTime, 0, exampleTime.length, rect)
        mTimeTextWidth = mTimeTextPaint!!.measureText(exampleTime)
        mTimeTextHeight = rect.height().toFloat()
        mHeaderMarginBottom = mTimeTextHeight / 2
        initTextTimeWidth()

        // Measure settings for header row.
        mHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHeaderTextPaint!!.setColor(mHeaderColumnTextColor)
        mHeaderTextPaint!!.setTextAlign(Paint.Align.CENTER)
        mHeaderTextPaint!!.setTextSize(mTextSize.toFloat())
        mHeaderTextPaint!!.getTextBounds(exampleTime, 0, exampleTime.length, rect)
        mHeaderTextHeight = rect.height().toFloat()
        mHeaderTextPaint!!.setTypeface(mTypeface)


        // Prepare header background paint.
        mHeaderBackgroundPaint = Paint()
        mHeaderBackgroundPaint!!.setColor(mHeaderRowBackgroundColor)

        // Prepare day background color paint.
        mDayBackgroundPaint = Paint()
        mDayBackgroundPaint!!.setColor(mDayBackgroundColor)
        mFutureBackgroundPaint = Paint()
        mFutureBackgroundPaint!!.setColor(mFutureBackgroundColor)
        mPastBackgroundPaint = Paint()
        mPastBackgroundPaint!!.setColor(mPastBackgroundColor)
        mFutureWeekendBackgroundPaint = Paint()
        mFutureWeekendBackgroundPaint!!.setColor(mFutureWeekendBackgroundColor)
        mPastWeekendBackgroundPaint = Paint()
        mPastWeekendBackgroundPaint!!.setColor(mPastWeekendBackgroundColor)

        // Prepare hour separator color paint.
        mHourSeparatorPaint = Paint()
        mHourSeparatorPaint!!.setStyle(Paint.Style.STROKE)
        mHourSeparatorPaint!!.setStrokeWidth(mHourSeparatorHeight.toFloat())
        mHourSeparatorPaint!!.setColor(mHourSeparatorColor)

        // Prepare the "now" line color paint
        mNowLinePaint = Paint()
        mNowLinePaint!!.setStrokeWidth(mNowLineThickness.toFloat())
        mNowLinePaint!!.setColor(mNowLineColor)

        // Prepare today background color paint.
        mTodayBackgroundPaint = Paint()
        mTodayBackgroundPaint!!.setColor(mTodayBackgroundColor)

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTodayHeaderTextPaint!!.setTextAlign(Paint.Align.CENTER)
        mTodayHeaderTextPaint!!.setTextSize(mTextSize.toFloat())
        mTodayHeaderTextPaint!!.setTypeface(mTypeface)
        mTodayHeaderTextPaint!!.setColor(mTodayHeaderTextColor)

        // Prepare event background color.
        mEventBackgroundPaint = Paint()
        mEventBackgroundPaint!!.setColor(Color.rgb(174, 208, 238))
        // Prepare empty event background color.
        val mNewEventBackgroundPaint = Paint()
        mNewEventBackgroundPaint.setColor(Color.rgb(60, 147, 217))

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = Paint()
        mHeaderColumnBackgroundPaint!!.setColor(mHeaderColumnBackgroundColor)

        // Prepare event text size and color.
        mEventTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
        mEventTextPaint!!.style = Paint.Style.FILL
        mEventTextPaint!!.color = mEventTextColor
        mEventTextPaint!!.textSize = mEventTextSize.toFloat()

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7")
        // Set default empty event color.
        mNewEventColor = Color.parseColor("#3c93d9")
        mScaleDetector = ScaleGestureDetector(mContext, WeekViewGestureListener())
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        mTimeTextWidth = 0f
        for (i in 0 until getNumberOfPeriods()) {
            // Measure time string and get max width.
            val time = dayTimeInterpreter!!.interpretTime(i, i % 2 * 30)
                ?: throw IllegalStateException("A DayTimeInterpreter must not return null time")
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint!!.measureText(time))
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: WeekViewEvent, event2: WeekViewEvent): Boolean {
        val start1: Long = event1.startTime!!.toNumericalUnit()
        val end1: Long = event1.endTime!!.toNumericalUnit()
        val start2: Long = event2.startTime!!.toNumericalUnit()
        val end2: Long = event2.endTime!!.toNumericalUnit()
        val minOverlappingMillis = mMinOverlappingMinutes * 60 * 1000.toLong()
        return !(start1 + minOverlappingMillis >= end2 || end1 <= start2 + minOverlappingMillis)
    }

    /**
     * limit current time of event by update mMinTime & mMaxTime
     * find smallest of start time & latest of end time
     */
    private fun limitEventTime(days: List<DayOfWeek?>) {
        if (mEventRects != null && mEventRects!!.size > 0) {
            var startTime: DayTime? = null
            var endTime: DayTime? = null
            for (eventRect in mEventRects!!) {
                for (day in days) {
                    if (eventRect.event.startTime!!.getDay() === day && !eventRect.event.isAllDay) {
                        if (startTime == null || getPassedMinutesInDay(startTime) > getPassedMinutesInDay(
                                eventRect.event.startTime!!
                            )) {
                            startTime = eventRect.event.startTime
                        }
                        if (endTime == null || getPassedMinutesInDay(endTime) < getPassedMinutesInDay(
                                eventRect.event
                                    .endTime!!
                            )) {
                            endTime = eventRect.event.endTime
                        }
                    }
                }
            }
            if (startTime != null && endTime != null && startTime.isBefore(endTime)) {
                setLimitTime(Math.max(0, startTime.hour), Math.min(24, endTime.hour + 1))
            }
        }
    }

    private fun recalculateHourHeight() {
        val height = ((height - (mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 +
                mHeaderMarginBottom)) / (mMaxTime - mMinTime)).toInt()
        if (height > mHourHeight) {
            if (height > mMaxHourHeight) {
                mMaxHourHeight = height
            }
            mNewHourHeight = height
        }
    }

    private fun resetHomeDay() {
        var newHomeDay: DayOfWeek = now.dayOfWeek
        if (mMinDay != null && newHomeDay < mMinDay) {
            newHomeDay = mMinDay as DayOfWeek
        }
        if (mMaxDay != null && newHomeDay > mMaxDay) {
            newHomeDay = mMaxDay as DayOfWeek
        }
        if (mMaxDay != null) {
            var day: DayOfWeek = mMaxDay!!.plus((1 - getRealNumberOfVisibleDays()).toLong())
            while (day < mMinDay) {
                day = day.plus(1)
            }
            if (newHomeDay > day) {
                newHomeDay = day
            }
        }
        mHomeDay = newHomeDay
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param eventRects The events to be sorted.
     */
    private fun sortEventRects(eventRects: List<EventRect>?) {
        Collections.sort(
            eventRects
        ) { left, right ->
            val start1: Long = left!!.event.startTime!!.toNumericalUnit()
            val start2: Long = right!!.event.startTime!!.toNumericalUnit()
            var comparator = start1.compareTo(start2)
            if (comparator == 0) {
                val end1: Long = left.event.endTime!!.toNumericalUnit()
                val end2: Long = right.event.endTime!!.toNumericalUnit()
                comparator = end1.compareTo(end2)
            }
            comparator
        }
    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas)
    }

    // fix rotation changes
    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAreDimensionsInvalid = true
    }

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private inner class DragListener : OnDragListener {
        override fun onDrag(v: View?, e: DragEvent): Boolean {
            when (e.action) {
                DragEvent.ACTION_DROP -> if (e.x > mHeaderColumnWidth && e.y > mHeaderTextHeight + mHeaderRowPadding * 2 +
                    mHeaderMarginBottom
                ) {
                    val selectedTime: DayTime? = getTimeFromPoint(e.x, e.y)
                    if (selectedTime != null) {
                        mDropListener!!.onDrop(v, selectedTime)
                    }
                }
            }
            return true
        }
    }

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private inner class EventRect
    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF The rectangle.
     */ internal constructor(
        var event: WeekViewEvent,
        var originalEvent: WeekViewEvent,
        var rectF: RectF?
    ) {
        var left = 0f
        var width = 0f
        var top = 0f
        var bottom = 0f
    }

    /**
     * A simple GestureListener that holds the focused hour while scaling.
     */
    private inner class WeekViewGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        var mFocusedPointY = 0f
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            mNewHourHeight = Math.round(mHourHeight * scale)

            // Calculating difference
            var diffY = mFocusedPointY - mCurrentOrigin.y
            // Scaling difference
            diffY = diffY * scale - diffY
            // Updating week view origin
            mCurrentOrigin.y -= diffY
            invalidate()
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mIsZooming = true
            goToNearestOrigin()

            // Calculate focused point for scale action
            mFocusedPointY = if (mZoomFocusPointEnabled) {
                // Use fractional focus, percentage of height
                (getHeight() - mHeaderHeight - mHeaderRowPadding * 2 - mHeaderMarginBottom) *
                        mZoomFocusPoint
            } else {
                // Grab focus
                detector.focusY
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mIsZooming = false
        }
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to setting and getting the properties.
    //
    /////////////////////////////////////////////////////////////////
    var columnGap: Int
        get() = mColumnGap
        set(columnGap) {
            mColumnGap = columnGap
            invalidate()
        }

    var dayBackgroundColor: Int
        get() = mDayBackgroundColor
        set(dayBackgroundColor) {
            mDayBackgroundColor = dayBackgroundColor
            mDayBackgroundPaint!!.setColor(mDayBackgroundColor)
            invalidate()
        }
    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The day, time interpreter.
     */// Refresh time column width.
    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dayTimeInterpreter The day, time interpreter.
     */
    var dayTimeInterpreter: DayTimeInterpreter? = null
        get() {
            if (mDayTimeInterpreter == null) {
                mDayTimeInterpreter = object : DayTimeInterpreter {
                    override fun interpretDay(day: Int): String {
                        val dayOfWeek: DayOfWeek = DayOfWeek.of(day)
                        return if (mNumberOfVisibleDays > 3) dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        ) else dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    }

                    override fun interpretTime(hour: Int, minutes: Int): String {
                        val time: LocalTime = LocalTime.of(hour, minutes)
                        return time.format(
                            if (DateFormat.is24HourFormat(getContext())) DateTimeFormatter.ofPattern(
                                "H"
                            ) else DateTimeFormatter.ofPattern("ha")
                        )
                    }
                }
            }
            return mDayTimeInterpreter
        }

    fun getDefaultEventColor(): Int {
        return mDefaultEventColor
    }

    fun setDefaultEventColor(defaultEventColor: Int) {
        mDefaultEventColor = defaultEventColor
        invalidate()
    }

    fun getEmptyViewClickListener(): EmptyViewClickListener? {
        return mEmptyViewClickListener
    }

    fun setEmptyViewClickListener(emptyViewClickListener: EmptyViewClickListener?) {
        mEmptyViewClickListener = emptyViewClickListener
    }

    fun getEmptyViewLongPressListener(): EmptyViewLongPressListener? {
        return mEmptyViewLongPressListener
    }

    fun setEmptyViewLongPressListener(emptyViewLongPressListener: EmptyViewLongPressListener?) {
        mEmptyViewLongPressListener = emptyViewLongPressListener
    }

    fun getEventClickListener(): EventClickListener? {
        return mEventClickListener
    }

    fun getEventCornerRadius(): Int {
        return mEventCornerRadius
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    fun setEventCornerRadius(eventCornerRadius: Int) {
        mEventCornerRadius = eventCornerRadius
    }

    fun getEventLongPressListener(): EventLongPressListener? {
        return mEventLongPressListener
    }

    fun setEventLongPressListener(eventLongPressListener: EventLongPressListener?) {
        mEventLongPressListener = eventLongPressListener
    }

    fun getEventMarginVertical(): Int {
        return mEventMarginVertical
    }

    /**
     * Set the top and bottom margin of the event. The event will release this margin from the top
     * and bottom edge. This margin is useful for differentiation consecutive events.
     *
     * @param eventMarginVertical The top and bottom margin.
     */
    fun setEventMarginVertical(eventMarginVertical: Int) {
        mEventMarginVertical = eventMarginVertical
        invalidate()
    }

    fun getEventPadding(): Int {
        return mEventPadding
    }

    fun setEventPadding(eventPadding: Int) {
        mEventPadding = eventPadding
        invalidate()
    }

    fun getEventTextColor(): Int {
        return mEventTextColor
    }

    fun setEventTextColor(eventTextColor: Int) {
        mEventTextColor = eventTextColor
        mEventTextPaint!!.color = mEventTextColor
        invalidate()
    }

    fun getEventTextSize(): Int {
        return mEventTextSize
    }

    fun setEventTextSize(eventTextSize: Int) {
        mEventTextSize = eventTextSize
        mEventTextPaint!!.textSize = mEventTextSize.toFloat()
        invalidate()
    }

    private fun getEventsTop(): Float {
        // Calculate top.
        return mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom + mTimeTextHeight / 2 +
                mEventMarginVertical - getMinHourOffset()
    }

    fun getFirstDayOfWeek(): DayOfWeek {
        return mFirstDayOfWeek
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     *
     * @param firstDayOfWeek The supported values are [DayOfWeek].
     */
    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        setFirstDayOfWeek(DayOfWeek.of(firstDayOfWeek))
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    fun getFirstVisibleDay(): DayOfWeek? {
        return mFirstVisibleDay
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    fun getFirstVisibleHour(): Double {
        return (-mCurrentOrigin.y / mHourHeight).toDouble()
    }

    fun getFutureBackgroundColor(): Int {
        return mFutureBackgroundColor
    }

    fun setFutureBackgroundColor(futureBackgroundColor: Int) {
        mFutureBackgroundColor = futureBackgroundColor
        mFutureBackgroundPaint!!.setColor(mFutureBackgroundColor)
    }

    fun getFutureWeekendBackgroundColor(): Int {
        return mFutureWeekendBackgroundColor
    }

    fun setFutureWeekendBackgroundColor(futureWeekendBackgroundColor: Int) {
        mFutureWeekendBackgroundColor = futureWeekendBackgroundColor
        mFutureWeekendBackgroundPaint!!.setColor(mFutureWeekendBackgroundColor)
    }

    fun getHeaderColumnBackgroundColor(): Int {
        return mHeaderColumnBackgroundColor
    }

    fun setHeaderColumnBackgroundColor(headerColumnBackgroundColor: Int) {
        mHeaderColumnBackgroundColor = headerColumnBackgroundColor
        mHeaderColumnBackgroundPaint!!.setColor(mHeaderColumnBackgroundColor)
        invalidate()
    }

    fun getHeaderColumnPadding(): Int {
        return mHeaderColumnPadding
    }

    fun setHeaderColumnPadding(headerColumnPadding: Int) {
        mHeaderColumnPadding = headerColumnPadding
        invalidate()
    }

    fun getHeaderColumnTextColor(): Int {
        return mHeaderColumnTextColor
    }

    fun setHeaderColumnTextColor(headerColumnTextColor: Int) {
        mHeaderColumnTextColor = headerColumnTextColor
        mHeaderTextPaint!!.setColor(mHeaderColumnTextColor)
        mTimeTextPaint!!.setColor(mHeaderColumnTextColor)
        invalidate()
    }

    fun getHeaderRowBackgroundColor(): Int {
        return mHeaderRowBackgroundColor
    }

    fun setHeaderRowBackgroundColor(headerRowBackgroundColor: Int) {
        mHeaderRowBackgroundColor = headerRowBackgroundColor
        mHeaderBackgroundPaint!!.setColor(mHeaderRowBackgroundColor)
        invalidate()
    }

    fun getHeaderRowPadding(): Int {
        return mHeaderRowPadding
    }

    fun setHeaderRowPadding(headerRowPadding: Int) {
        mHeaderRowPadding = headerRowPadding
        invalidate()
    }

    fun getHourHeight(): Int {
        return mHourHeight
    }

    fun setHourHeight(hourHeight: Int) {
        mNewHourHeight = hourHeight
        invalidate()
    }

    fun getHourSeparatorColor(): Int {
        return mHourSeparatorColor
    }

    fun setHourSeparatorColor(hourSeparatorColor: Int) {
        mHourSeparatorColor = hourSeparatorColor
        mHourSeparatorPaint!!.setColor(mHourSeparatorColor)
        invalidate()
    }

    fun getHourSeparatorHeight(): Int {
        return mHourSeparatorHeight
    }

    fun setHourSeparatorHeight(hourSeparatorHeight: Int) {
        mHourSeparatorHeight = hourSeparatorHeight
        mHourSeparatorPaint!!.setStrokeWidth(mHourSeparatorHeight.toFloat())
        invalidate()
    }

    /**
     * Returns the last visible day in the week view.
     *
     * @return The last visible day in the week view.
     */
    fun getLastVisibleDay(): DayOfWeek? {
        return mLastVisibleDay
    }

    private fun getLeftDaysWithGaps(): Int {
        return (-Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap).toDouble())).toInt()
    }

    /**
     * Get the latest day that can be displayed. Will return null if no maximum date is set.
     *
     * @return the latest day the can be displayed, null if no max day set
     */
    fun getMaxDay(): DayOfWeek? {
        return mMaxDay
    }

    /**
     * Set the latest day that can be displayed. This will determine the right horizontal scroll
     * limit. The default value is null (allow unlimited scrolling in to the future).
     *
     * @param maxDay The new maximum day (pass null for no maximum)
     */
    fun setMaxDay(maxDay: DayOfWeek?) {
        if (maxDay != null) {
            require(!(mMinDay != null && maxDay.compareTo(mMinDay) < 0)) { "maxDay has to be after minDay" }
        }
        mMaxDay = maxDay
        resetHomeDay()
        mCurrentOrigin.x = 0f
        invalidate()
    }

    fun getMaxHourHeight(): Int {
        return mMaxHourHeight
    }

    fun setMaxHourHeight(maxHourHeight: Int) {
        mMaxHourHeight = maxHourHeight
    }

    fun getMaxTime(): Int {
        return mMaxTime
    }

    /**
     * Set highest shown time
     *
     * @param endHour limit time display at bottom (between 0~24 and larger than startHour)
     */
    fun setMaxTime(endHour: Int) {
        require(endHour > mMinTime) { "endHour must larger startHour." }
        require(endHour <= 24) { "endHour can't be higher than 24." }
        mMaxTime = endHour
        recalculateHourHeight()
        invalidate()
    }

    /**
     * Get the earliest day that can be displayed. Will return null if no minimum day is set.
     *
     * @return the earliest day that can be displayed, null if no minimum day set
     */
    fun getMinDay(): DayOfWeek? {
        return mMinDay
    }

    /**
     * Set the earliest day that can be displayed. This will determine the left horizontal scroll
     * limit. The default value is null (allow unlimited scrolling into the past).
     *
     * @param minDay The new minimum day (pass null for no minimum)
     */
    fun setMinDay(minDay: DayOfWeek?) {
        if (minDay != null) {
            require(!(mMaxDay != null && minDay.compareTo(mMaxDay) > 0)) { "minDay cannot be later than maxDay" }
        }
        mMinDay = minDay
        resetHomeDay()
        mCurrentOrigin.x = 0f
        invalidate()
    }

    fun getMinHourHeight(): Int {
        return mMinHourHeight
    }

    fun setMinHourHeight(minHourHeight: Int) {
        mMinHourHeight = minHourHeight
    }

    private fun getMinHourOffset(): Int {
        return mHourHeight * mMinTime
    }

    fun getMinOverlappingMinutes(): Int {
        return mMinOverlappingMinutes
    }

    fun setMinOverlappingMinutes(minutes: Int) {
        mMinOverlappingMinutes = minutes
    }

    fun getMinTime(): Int {
        return mMinTime
    }

    /**
     * Set minimal shown time
     *
     * @param startHour limit time display on top (between 0~24) and smaller than endHour
     */
    fun setMinTime(startHour: Int) {
        require(mMaxTime > startHour) { "startHour must smaller than endHour" }
        require(startHour >= 0) { "startHour must be at least 0." }
        mMinTime = startHour
        recalculateHourHeight()
    }

    fun getNewEventColor(): Int {
        return mNewEventColor
    }

    fun setNewEventColor(defaultNewEventColor: Int) {
        mNewEventColor = defaultNewEventColor
        invalidate()
    }

    fun getNewEventIconDrawable(): Drawable? {
        return mNewEventIconDrawable
    }

    fun setNewEventIconDrawable(newEventIconDrawable: Drawable?) {
        mNewEventIconDrawable = newEventIconDrawable
    }

    fun getNewEventIdentifier(): String? {
        return mNewEventIdentifier
    }

    fun setNewEventIdentifier(newEventId: String?) {
        mNewEventIdentifier = newEventId
    }

    fun getNewEventLengthInMinutes(): Int {
        return mNewEventLengthInMinutes
    }

    fun setNewEventLengthInMinutes(newEventLengthInMinutes: Int) {
        mNewEventLengthInMinutes = newEventLengthInMinutes
    }

    fun getNewEventTimeResolutionInMinutes(): Int {
        return mNewEventTimeResolutionInMinutes
    }

    fun setNewEventTimeResolutionInMinutes(newEventTimeResolutionInMinutes: Int) {
        mNewEventTimeResolutionInMinutes = newEventTimeResolutionInMinutes
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    fun getNowLineColor(): Int {
        return mNowLineColor
    }

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    fun setNowLineColor(nowLineColor: Int) {
        mNowLineColor = nowLineColor
        invalidate()
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    fun getNowLineThickness(): Int {
        return mNowLineThickness
    }

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    fun setNowLineThickness(nowLineThickness: Int) {
        mNowLineThickness = nowLineThickness
        invalidate()
    }

    private fun getNumberOfPeriods(): Int {
        return ((mMaxTime - mMinTime) * (60.0 / mTimeColumnResolution)).toInt()
    }

    /**
     * Get the number of visible days
     *
     * @return The set number of visible days.
     */
    fun getNumberOfVisibleDays(): Int {
        return mNumberOfVisibleDays
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    fun setNumberOfVisibleDays(numberOfVisibleDays: Int) {
        mNumberOfVisibleDays = numberOfVisibleDays
        resetHomeDay()
        mCurrentOrigin.x = 0f
        mCurrentOrigin.y = 0f
        invalidate()
    }

    fun getOverlappingEventGap(): Int {
        return mOverlappingEventGap
    }

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    fun setOverlappingEventGap(overlappingEventGap: Int) {
        mOverlappingEventGap = overlappingEventGap
        invalidate()
    }

    fun getPastBackgroundColor(): Int {
        return mPastBackgroundColor
    }

    fun setPastBackgroundColor(pastBackgroundColor: Int) {
        mPastBackgroundColor = pastBackgroundColor
        mPastBackgroundPaint!!.setColor(mPastBackgroundColor)
    }

    fun getPastWeekendBackgroundColor(): Int {
        return mPastWeekendBackgroundColor
    }

    fun setPastWeekendBackgroundColor(pastWeekendBackgroundColor: Int) {
        mPastWeekendBackgroundColor = pastWeekendBackgroundColor
        mPastWeekendBackgroundPaint!!.setColor(mPastWeekendBackgroundColor)
    }

    /**
     * Get the real number of visible days
     * If the amount of days between max day and min day is smaller, that value is returned
     *
     * @return The real number of visible days
     */
    fun getRealNumberOfVisibleDays(): Int {
        return if (mMinDay == null || mMaxDay == null) {
            getNumberOfVisibleDays()
        } else Math.min(mNumberOfVisibleDays, daysBetween(mMinDay!!, mMaxDay!!) + 1)
    }

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    fun getScrollDuration(): Int {
        return mScrollDuration
    }

    /**
     * Set the scroll duration
     *
     * @param scrollDuration the new scrollDuraction
     */
    fun setScrollDuration(scrollDuration: Int) {
        mScrollDuration = scrollDuration
    }

    fun getTextSize(): Int {
        return mTextSize
    }

    fun setTextSize(textSize: Int) {
        mTextSize = textSize
        mTodayHeaderTextPaint!!.setTextSize(mTextSize.toFloat())
        mHeaderTextPaint!!.setTextSize(mTextSize.toFloat())
        mTimeTextPaint!!.setTextSize(mTextSize.toFloat())
        invalidate()
    }

    fun getTimeColumnResolution(): Int {
        return mTimeColumnResolution
    }

    fun setTimeColumnResolution(resolution: Int) {
        mTimeColumnResolution = resolution
    }

    fun getTodayBackgroundColor(): Int {
        return mTodayBackgroundColor
    }

    fun setTodayBackgroundColor(todayBackgroundColor: Int) {
        mTodayBackgroundColor = todayBackgroundColor
        mTodayBackgroundPaint!!.setColor(mTodayBackgroundColor)
        invalidate()
    }

    fun getTodayHeaderTextColor(): Int {
        return mTodayHeaderTextColor
    }

    fun setTodayHeaderTextColor(todayHeaderTextColor: Int) {
        mTodayHeaderTextColor = todayHeaderTextColor
        mTodayHeaderTextPaint!!.setColor(mTodayHeaderTextColor)
        invalidate()
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    fun getWeekViewLoader(): WeekViewLoader? {
        return mWeekViewLoader
    }

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param loader The event loader.
     */
    fun setWeekViewLoader(loader: WeekViewLoader?) {
        mWeekViewLoader = loader
    }

    private fun getXMaxLimit(): Float {
        return if (mMinDay == null) {
            Int.MAX_VALUE.toFloat()
        } else {
            getXOriginForDay(mMinDay!!)
        }
    }

    private fun getXMinLimit(): Float {
        return if (mMaxDay == null) {
            Int.MIN_VALUE.toFloat()
        } else {
            var day: DayOfWeek = mMaxDay!!.plus((1 - getRealNumberOfVisibleDays()).toLong())
            while (day < mMinDay) {
                day = day.plus(1)
            }
            getXOriginForDay(day)
        }
    }

    /**
     * Get the scrolling speed factor in horizontal direction.
     *
     * @return The speed factor in horizontal direction.
     */
    fun getXScrollingSpeed(): Float {
        return mXScrollingSpeed
    }

    /**
     * Sets the speed for horizontal scrolling.
     *
     * @param xScrollingSpeed The new horizontal scrolling speed.
     */
    fun setXScrollingSpeed(xScrollingSpeed: Float) {
        mXScrollingSpeed = xScrollingSpeed
    }

    private fun getXStartPixel(): Float {
        return mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * getLeftDaysWithGaps() +
                mHeaderColumnWidth
    }

    private fun getYMaxLimit(): Float {
        return 0F
    }

    private fun getYMinLimit(): Float {
        return -((mHourHeight * (mMaxTime - mMinTime) + mHeaderHeight
                + mHeaderRowPadding * 2 + mHeaderMarginBottom
                + mTimeTextHeight / 2)
                - getHeight())
    }

    /*
     * Get focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     * @return focus point
     */
    fun getZoomFocusPoint(): Float {
        return mZoomFocusPoint
    }

    /**
     * Set focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     *
     * @param zoomFocusPoint the new zoomFocusPoint
     */
    fun setZoomFocusPoint(zoomFocusPoint: Float) {
        check(!(0 > zoomFocusPoint || zoomFocusPoint > 1)) { "The zoom focus point percentage has to be between 0 and 1" }
        mZoomFocusPoint = zoomFocusPoint
    }

    fun isDropListenerEnabled(): Boolean {
        return mEnableDropListener
    }

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    fun isHorizontalFlingEnabled(): Boolean {
        return mHorizontalFlingEnabled
    }

    /**
     * Set whether the week view should fling horizontally.
     *
     * @param enabled whether the week view should fling horizontally
     */
    fun setHorizontalFlingEnabled(enabled: Boolean) {
        mHorizontalFlingEnabled = enabled
    }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    fun isShowDistinctPastFutureColor(): Boolean {
        return mShowDistinctPastFutureColor
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     * background colors.
     */
    fun setShowDistinctPastFutureColor(showDistinctPastFutureColor: Boolean) {
        mShowDistinctPastFutureColor = showDistinctPastFutureColor
        invalidate()
    }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    fun isShowDistinctWeekendColor(): Boolean {
        return mShowDistinctWeekendColor
    }

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    fun setShowDistinctWeekendColor(showDistinctWeekendColor: Boolean) {
        mShowDistinctWeekendColor = showDistinctWeekendColor
        invalidate()
    }

    fun isShowFirstDayOfWeekFirst(): Boolean {
        return mShowFirstDayOfWeekFirst
    }

    fun setShowFirstDayOfWeekFirst(show: Boolean) {
        mShowFirstDayOfWeekFirst = show
    }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    fun isShowNowLine(): Boolean {
        return mShowNowLine
    }

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    fun setShowNowLine(showNowLine: Boolean) {
        mShowNowLine = showNowLine
        invalidate()
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    fun isVerticalFlingEnabled(): Boolean {
        return mVerticalFlingEnabled
    }

    /**
     * Set whether the week view should fling vertically.
     *
     * @param enabled whether the week view should fling vertically
     */
    fun setVerticalFlingEnabled(enabled: Boolean) {
        mVerticalFlingEnabled = enabled
    }

    /*
     * Is focus point enabled
     * @return fixed focus point enabled?
     */
    fun isZoomFocusPointEnabled(): Boolean {
        return mZoomFocusPointEnabled
    }

    /**
     * Enable zoom focus point
     * If you set this to false the `zoomFocusPoint` won't take effect any more while zooming.
     * The zoom will always be focused at the center of your gesture.
     *
     * @param zoomFocusPointEnabled whether the zoomFocusPoint is enabled
     */
    fun setZoomFocusPointEnabled(zoomFocusPointEnabled: Boolean) {
        mZoomFocusPointEnabled = zoomFocusPointEnabled
    }

    /**
     * auto calculate limit time on events in visible days.
     */
    fun setAutoLimitTime(isAuto: Boolean) {
        mAutoLimitTime = isAuto
        invalidate()
    }

    fun setDropListener(dropListener: DropListener?) {
        mDropListener = dropListener
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     *
     * @param firstDayOfWeek First day of the week.
     */
    fun setFirstDayOfWeek(firstDayOfWeek: DayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek
        invalidate()
    }

    fun setOnEventClickListener(listener: EventClickListener?) {
        mEventClickListener = listener
    }

    fun setTypeface(typeface: Typeface?) {
        if (typeface != null) {
            mEventTextPaint!!.typeface = typeface
            mTodayHeaderTextPaint!!.setTypeface(typeface)
            mTimeTextPaint!!.setTypeface(typeface)
            mTypeface = typeface
            init()
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller!!.isFinished) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin()
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin()
            } else if (mScroller!!.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller!!.currY.toFloat()
                mCurrentOrigin.x = mScroller!!.currX.toFloat()
                postInvalidateOnAnimation()
            }
        }
    }

    /**
     * Determine whether a given calendar day falls within the scroll limits set for this view.
     *
     * @param day the day to check
     * @return True if there are no limit or the day is within the limits.
     * @see .setMinDay
     * @see .setMaxDay
     */
    fun dayIsValid(day: DayOfWeek?): Boolean {
        return (mMinDay == null || day!!.compareTo(mMinDay) >= 0) && (mMaxDay == null || day!!.compareTo(
            mMaxDay
        ) <= 0)
    }

    fun disableDropListener() {
        mEnableDropListener = false
        setOnDragListener(null)
    }

    fun enableDropListener() {
        mEnableDropListener = true
        setOnDragListener(DragListener())
    }

    /**
     * Show a specific day on the week view.
     *
     * @param day The day to show.
     */
    fun goToDay(day: DayOfWeek?) {
        mScroller!!.forceFinished(true)
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
        if (mAreDimensionsInvalid) {
            mScrollToDay = day
            return
        }
        mRefreshEvents = true
        mCurrentOrigin.x = -daysBetween(mHomeDay!!, day!!) * (mWidthPerDay + mColumnGap)
        invalidate()
    }

    /**
     * Show a specific day on the week view.
     *
     * @param day The day to show.
     */
    fun goToDay(day: Int) {
        goToDay(DayOfWeek.of(day))
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }
        var verticalOffset = 0
        if (hour > mMaxTime) {
            verticalOffset = mHourHeight * (mMaxTime - mMinTime)
        } else if (hour > mMinTime) {
            verticalOffset = (mHourHeight * hour).toInt()
        }
        if (verticalOffset > mHourHeight * (mMaxTime - mMinTime) - getHeight() + mHeaderHeight + mHeaderRowPadding *
            2 + mHeaderMarginBottom) {
            verticalOffset = (mHourHeight * (mMaxTime - mMinTime) - getHeight() + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom).toInt()
        }
        mCurrentOrigin.y = -verticalOffset.toFloat()
        invalidate()
    }

    /**
     * Scrolls the calendar to current day and time.
     */
    fun goToNow() {
        goToDay(now.getDayOfWeek())
        goToHour(now.getHour().toDouble())
    }

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        goToDay(now.getDayOfWeek())
    }

    override fun invalidate() {
        super.invalidate()
        mAreDimensionsInvalid = true
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDatasetChanged() {
        mRefreshEvents = true
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector!!.onTouchEvent(event)
        val `val` = mGestureDetector!!.onTouchEvent(event)

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            }
            mCurrentScrollDirection = Direction.NONE
        }
        return `val`
    }

    /**
     * Set visible time span.
     *
     * @param startHour limit time display on top (between 0~24)
     * @param endHour limit time display at bottom (between 0~24 and larger than startHour)
     */
    fun setLimitTime(startHour: Int, endHour: Int) {
        require(endHour > startHour) { "endHour must larger startHour." }
        require(startHour >= 0) { "startHour must be at least 0." }
        require(endHour <= 24) { "endHour can't be higher than 24." }
        mMinTime = startHour
        mMaxTime = endHour
        recalculateHourHeight()
        invalidate()
    }

    interface AddEventClickListener {
        /**
         * Triggered when the users clicks to create a new event.
         *
         * @param startTime The startTime of a new event
         * @param endTime The endTime of a new event
         */
        fun onAddEventClicked(startTime: DayTime?, endTime: DayTime?)
    }

    /**
     * Created by Raquib on 1/6/2015.
     */
    interface DayTimeInterpreter {
        fun interpretDay(day: Int): String?
        fun interpretTime(hour: Int, minutes: Int): String?
    }

    interface DropListener {
        /**
         * Triggered when view dropped
         *
         * @param view: dropped view.
         * @param day: object set with the day and time of the dropped coordinates on the view.
         */
        fun onDrop(view: View?, day: DayTime?)
    }

    interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         *
         * @param day: [DayTime] object set with the day and time of the clicked position on the view.
         */
        fun onEmptyViewClicked(day: DayTime?)
    }

    interface EmptyViewLongPressListener {
        /**
         * Similar to [me.jlurena.revolvingweekview.WeekView.EmptyViewClickListener] but with long press.
         *
         * @param time: [DayTime] object set with the day and time of the long pressed position on the view.
         */
        fun onEmptyViewLongPress(time: DayTime?)
    }

    interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventClick(event: WeekViewEvent?, eventRect: RectF?)
    }

    interface EventLongPressListener {
        /**
         * Similar to [me.jlurena.revolvingweekview.WeekView.EventClickListener] but with a long press.
         *
         * @param event: event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: WeekViewEvent?, eventRect: RectF?)
    }

    interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         *
         * (this will also be called during the first draw of the weekview)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        fun onFirstVisibleDayChanged(newFirstVisibleDay: DayOfWeek?, oldFirstVisibleDay: DayOfWeek?)
    }

    interface TextColorPicker {
        @ColorInt
        fun getTextColor(event: WeekViewEvent?): Int
    }

    interface WeekViewLoader {
        /**
         * Load the events within the period
         *
         * @return A list with the events of this period
         */
        fun onWeekViewLoad(): List<WeekViewEvent>
    }

    init {
        AndroidThreeTen.init(context)
        now = LocalDateTime.now()

        // Hold references.

        // Hold references.
        mContext = context

        // Get the attribute values (if any).

        // Get the attribute values (if any).
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = DayOfWeek.of(
                a.getInteger(
                    R.styleable.WeekView_firstDayOfWeek,
                    mFirstDayOfWeek.value
                )
            )
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight)
            mMinHourHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight)
            mEffectiveMinHourHeight = mMinHourHeight
            mMaxHourHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight)
            mTextSize = a.getDimensionPixelSize(
                R.styleable.WeekView_textSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    mTextSize.toFloat(),
                    context.resources.displayMetrics
                )
                    .toInt()
            )
            mHeaderColumnPadding = a.getDimensionPixelSize(
                R.styleable.WeekView_headerColumnPadding,
                mHeaderColumnPadding
            )
            mColumnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, mColumnGap)
            mHeaderColumnTextColor =
                a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor)
            mNumberOfVisibleDays =
                a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays)
            mShowFirstDayOfWeekFirst = a.getBoolean(
                R.styleable.WeekView_showFirstDayOfWeekFirst,
                mShowFirstDayOfWeekFirst
            )
            mHeaderRowPadding =
                a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding)
            mHeaderRowBackgroundColor = a.getColor(
                R.styleable.WeekView_headerRowBackgroundColor,
                mHeaderRowBackgroundColor
            )
            mDayBackgroundColor =
                a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor)
            mFutureBackgroundColor =
                a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor)
            mPastBackgroundColor =
                a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor)
            mFutureWeekendBackgroundColor = a.getColor(
                R.styleable.WeekView_futureWeekendBackgroundColor,
                mFutureBackgroundColor
            ) // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(
                R.styleable.WeekView_pastWeekendBackgroundColor,
                mPastBackgroundColor
            )
            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor)
            mNowLineThickness =
                a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness)
            mHourSeparatorColor =
                a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor)
            mTodayBackgroundColor =
                a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor)
            mHourSeparatorHeight = a.getDimensionPixelSize(
                R.styleable.WeekView_hourSeparatorHeight,
                mHourSeparatorHeight
            )
            mTodayHeaderTextColor =
                a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(
                R.styleable.WeekView_eventTextSize, TypedValue
                    .applyDimension(
                        TypedValue.COMPLEX_UNIT_SP, mEventTextSize.toFloat(), context.resources
                            .displayMetrics
                    ).toInt()
            )
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            mNewEventColor = a.getColor(R.styleable.WeekView_newEventColor, mNewEventColor)
            mNewEventIconDrawable = a.getDrawable(R.styleable.WeekView_newEventIconResource)
            mNewEventIdentifier =
                if (a.getString(R.styleable.WeekView_newEventIdentifier) != null) a.getString(R.styleable.WeekView_newEventIdentifier) else mNewEventIdentifier
            mNewEventLengthInMinutes =
                a.getInt(R.styleable.WeekView_newEventLengthInMinutes, mNewEventLengthInMinutes)
            mNewEventTimeResolutionInMinutes = a.getInt(
                R.styleable.WeekView_newEventTimeResolutionInMinutes,
                mNewEventTimeResolutionInMinutes
            )
            mEventPadding =
                a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            mHeaderColumnBackgroundColor = a.getColor(
                R.styleable.WeekView_headerColumnBackground,
                mHeaderColumnBackgroundColor
            )
            mOverlappingEventGap = a.getDimensionPixelSize(
                R.styleable.WeekView_overlappingEventGap,
                mOverlappingEventGap
            )
            mEventMarginVertical = a.getDimensionPixelSize(
                R.styleable.WeekView_eventMarginVertical,
                mEventMarginVertical
            )
            mXScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, mXScrollingSpeed)
            mEventCornerRadius =
                a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, mEventCornerRadius)
            mShowDistinctPastFutureColor = a.getBoolean(
                R.styleable.WeekView_showDistinctPastFutureColor,
                mShowDistinctPastFutureColor
            )
            mShowDistinctWeekendColor = a.getBoolean(
                R.styleable.WeekView_showDistinctWeekendColor,
                mShowDistinctWeekendColor
            )
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            mHorizontalFlingEnabled = a.getBoolean(
                R.styleable.WeekView_horizontalFlingEnabled,
                mHorizontalFlingEnabled
            )
            mVerticalFlingEnabled =
                a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, mVerticalFlingEnabled)
            allDayEventHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, allDayEventHeight)
            mZoomFocusPoint =
                a.getFraction(R.styleable.WeekView_zoomFocusPoint, 1, 1, mZoomFocusPoint)
            mZoomFocusPointEnabled =
                a.getBoolean(R.styleable.WeekView_zoomFocusPointEnabled, mZoomFocusPointEnabled)
            mScrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, mScrollDuration)
            mTimeColumnResolution =
                a.getInt(R.styleable.WeekView_timeColumnResolution, mTimeColumnResolution)
            mAutoLimitTime = a.getBoolean(R.styleable.WeekView_autoLimitTime, mAutoLimitTime)
            mMinTime = a.getInt(R.styleable.WeekView_minTime, mMinTime)
            mMaxTime = a.getInt(R.styleable.WeekView_maxTime, mMaxTime)
            if (a.getBoolean(R.styleable.WeekView_dropListenerEnabled, false)) {
                enableDropListener()
            }
            mMinOverlappingMinutes = a.getInt(R.styleable.WeekView_minOverlappingMinutes, 0)
        } finally {
            a.recycle()
        }
        init()
    }
}