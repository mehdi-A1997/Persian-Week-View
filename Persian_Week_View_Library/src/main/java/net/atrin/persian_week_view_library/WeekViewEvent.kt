package net.atrin.persian_week_view_library

import android.graphics.Shader
import androidx.annotation.ColorInt
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class WeekViewEvent {
    var identifier: String? = null
    var startTime: DayTime? = null
    var endTime: DayTime? = null
    var name: String? = null
    var location: String? = null

    @get:ColorInt
    @ColorInt
    var color = 0
    var isAllDay = false
    var shader: Shader? = null

    constructor() {}

    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event as String.
     * @param name Name of the event.
     * @param startDay Day when the event starts.
     * @param startHour Hour (in 24-hour format) when the event starts.
     * @param startMinute Minute when the event starts.
     * @param endDay Day when the event ends.
     * @param endHour Hour (in 24-hour format) when the event ends.
     * @param endMinute Minute when the event ends.
     */
    
    constructor(id: String?, name: String?, startDay: Int, startHour: Int, startMinute: Int, endDay: Int, endHour: Int, endMinute: Int) {
        identifier = id
        startTime = DayTime(startDay, startHour, startMinute)
        endTime = DayTime(endDay, endHour, endMinute)
        this.name = name
    }
    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event as String.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event.
     * @param shader the Shader of the event rectangle
     */
    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event as String.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event
     */
    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event as String.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    @JvmOverloads
    constructor(id: String?, name: String?, location: String?, startTime: DayTime?, endTime: DayTime?, allDay: Boolean = false,
                shader: Shader? = null) {
        identifier = id
        this.name = name
        this.location = location
        this.startTime = startTime
        this.endTime = endTime
        isAllDay = allDay
        this.shader = shader
    }

    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event.
     * @param name Name of the event.
     * @param location The location of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     * @param allDay Is the event an all day event.
     * @param shader the Shader of the event rectangle
     */
    @Deprecated("")
    constructor(id: Long, name: String?, location: String?, startTime: DayTime?, endTime: DayTime?, allDay: Boolean,
                shader: Shader?) : this(id.toString(), name, location, startTime, endTime, allDay, shader) {
    }

    /**
     * Initializes the event for week view.
     *
     * @param id The id of the event specified as String.
     * @param name Name of the event.
     * @param startTime The time when the event starts.
     * @param endTime The time when the event ends.
     */
    constructor(id: String?, name: String?, startTime: DayTime?, endTime: DayTime?) : this(id, name, null, startTime, endTime) {}

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as WeekViewEvent
        return identifier == that.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    
    fun splitWeekViewEvents(): List<WeekViewEvent> {
        //This function splits the WeekViewEvent in WeekViewEvents by day
        val events: MutableList<WeekViewEvent> = ArrayList()
        var endTime = DayTime(endTime!!)
        if (startTime!!.getDay() !== endTime.getDay()) {
            endTime = DayTime(startTime!!.getDay(), LocalTime.MAX)
            val event1 = WeekViewEvent(identifier, name, location, startTime, endTime,
                isAllDay)
            event1.color = color
            events.add(event1)

            // Add other days.
            val otherDay = DayTime(DayOfWeek.of(1), startTime!!.getTime())
            while (otherDay.getDay() !== this.endTime!!.getDay()) {
                val overDay = DayTime(otherDay.getDay(), LocalTime.MIN)
                val endOfOverDay = DayTime(overDay.getDay(), LocalTime.MAX)
                val eventMore = WeekViewEvent(identifier, name, null, overDay, endOfOverDay, isAllDay)
                eventMore.color = color
                events.add(eventMore)

                // Add next day.
                otherDay.addDays(1)
            }

            // Add last day.
            val startTime = DayTime(this.endTime!!.getDay(), LocalTime.MIN)
            val event2 = WeekViewEvent(identifier, name, location, startTime, this.endTime,
                isAllDay)
            event2.color = color
            events.add(event2)
        } else {
            events.add(this)
        }
        return events
    }

    override fun toString(): String {
        return "WeekViewEvent{" +
                "mId='" + identifier + '\'' +
                ", mStartTime=" + startTime +
                ", mEndTime=" + endTime +
                ", mName='" + name + '\'' +
                ", mLocation='" + location + '\'' +
                '}'
    }
}