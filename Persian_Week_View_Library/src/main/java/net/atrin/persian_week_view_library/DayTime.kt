package net.atrin.persian_week_view_library

import android.os.Build
import androidx.annotation.RequiresApi
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class DayTime : Comparable<DayTime?> {
    var time: LocalTime? = null
    private var day: DayOfWeek? = null

    constructor(dayTime: DayTime) {
        time = dayTime.time // LocalTime is immutable
        day = dayTime.day
    }

    constructor(day: DayOfWeek?, time: LocalTime?) {
        this.time = time
        this.day = day
    }

    /**
     * Create a DayTime object event.
     *
     * @param day A [day of the week][DayOfWeek].
     * @param hour Hour of day from 0-23.
     * @param minute Minute of day from 0-59.
     */
    constructor(day: DayOfWeek?, hour: Int, minute: Int) : this(day, LocalTime.of(hour, minute)) {}

    /**
     * Create a DayTime object event.
     *
     * @param day Integer representing a day of the week based on [DayOfWeek]
     * @param hour Hour of day from 0-23.
     * @param minute Minute of day from 0-59.
     */
    constructor(day: Int, hour: Int, minute: Int) : this(DayOfWeek.of(day), hour, minute) {}

    /**
     * Default constructor with no parameters.
     */
    constructor() {}
    constructor(localDateTime: LocalDateTime) {
        day = localDateTime.dayOfWeek
        time = localDateTime.toLocalTime()
    }

    fun getDay(): DayOfWeek? {
        return day
    }

    fun setDay(day: DayOfWeek?) {
        this.day = day
    }

    /**
     * Day of week integer value. Values are described in [DayOfWeek]
     *
     * @return Integer value of the day of the week.
     */
    val dayValue: Int
        get() = day!!.getValue()

    /**
     * Get hour in time
     *
     * @return Hour in time.
     */
    val hour: Int
        get() = time!!.getHour()

    /**
     * Get minute in time.
     *
     * @return Minute in time.
     */
    val minute: Int
        get() = time!!.getMinute()

    @JvmName("getTime1")
    fun getTime(): LocalTime? {
        return time
    }

    @JvmName("setTime1")
    fun setTime(time: LocalTime?) {
        this.time = time
    }

    /**
     * Sets the day of week based on values in #[DayOfWeek].
     *
     * @param day Integer representing the day of the week.
     */
    fun setDay(day: Int) {
        this.day = DayOfWeek.of(day)
    }

    /**
     * Adds days to this DayTime object.
     *
     * @param days Days to add.
     */
    fun addDays(days: Int) {
        day = day!!.plus(days.toLong())
    }

    /**
     * Adds hours to this time.
     *
     * @param hours Hours to add.
     */
    
    fun addHours(hours: Int) {
        time = time!!.plusHours(hours.toLong())
    }

    /**
     * Adds minutes to this time.
     *
     * @param minutes Minutes to add.
     */
    
    fun addMinutes(minutes: Int) {
        time = time!!.plusMinutes(minutes.toLong())
    }


    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val dayTime = o as DayTime
        return Objects.equals(time, dayTime.time) &&
                day === dayTime.day
    }

    override fun hashCode(): Int {
        return Objects.hash(time, day)
    }

    /**
     * Check if this DayTime is after.
     *
     * @param otherDayTime Other DayTime to compare to.
     * @return true if it is after else false.
     */
    fun isAfter(otherDayTime: DayTime?): Boolean {
        return this.compareTo(otherDayTime) > 0
    }

    /**
     * Check if this DayTime is before.
     *
     * @param otherDayTime Other DayTime to compare to.
     * @return true if it is before else false.
     */
    fun isBefore(otherDayTime: DayTime?): Boolean {
        return this.compareTo(otherDayTime) < 0
    }

    /**
     * Check if this DayTime is same.
     *
     * @param otherDayTime Other DayTime to compare to.
     * @return true if it is same time else false.
     */
    fun isSame(otherDayTime: DayTime?): Boolean {
        return this.compareTo(otherDayTime) == 0
    }

    /**
     * Sets time to minimum time of 0:00.
     */
    
    fun setMinimumTime() {
        time = LocalTime.MIN
    }

    
    fun setTime(hour: Int, minute: Int) {
        time = LocalTime.of(hour, minute)
    }

    /**
     * Subtracts minutes from this time.
     *
     * @param minutes Minutes to substract.
     */
    
    fun subtractMinutes(minutes: Int) {
        time = time!!.minusMinutes(minutes.toLong())
    }

    /**
     * Get this DayTime object as a numerical unit of NanoSeconds + DayValue.
     *
     * @return A number representing the day.
     */
    
    fun toNumericalUnit(): Long {
        return getTime()!!.toNanoOfDay() + getDay()!!.getValue()
    }

    
    override fun toString(): String {
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("K:ha")
        return "DayTime{" +
                "time=" + time!!.format(dtf) +
                ", day=" + day +
                '}'
    }

    override fun compareTo(dayTime: DayTime?): Int {
        return if (day === dayTime!!.day) {
            time!!.compareTo(dayTime!!.time)
        } else {
            day!!.compareTo(dayTime!!.day)
        }
    }
}