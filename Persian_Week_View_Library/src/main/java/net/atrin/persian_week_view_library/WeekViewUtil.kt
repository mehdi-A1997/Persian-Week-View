package net.atrin.persian_week_view_library

import org.threeten.bp.DayOfWeek

object WeekViewUtil {
    /**
     * [DayOfWeek] day of week integer value to [java.util.Calendar.DAY_OF_WEEK] integer value.
     *
     * @param dayOfWeek The [DayOfWeek] integer value representing the day of the week.
     * @return Integer value representing Day Of Week per [java.util.Calendar] standards.
     */
    fun dayOfWeekToCalendarDay(dayOfWeek: Int): Int {
        return dayOfWeek % 7 + 1
    }

    /**
     * [java.util.Calendar.DAY_OF_WEEK] integer value to [DayOfWeek] integer value.
     * @param calendarDay The [java.util.Calendar] integer value representing the day of the week.
     * @return The correct [DayOfWeek] integer value.
     */
    fun calendarDayToDayOfWeek(calendarDay: Int): Int {
        return if (calendarDay == 1) 7 else calendarDay - 1
    }

    /**
     * Returns the amount of days between the second date and the first date
     *
     * @param dateOne the first date
     * @param dateTwo the second date
     * @return the amount of days between dateTwo and dateOne
     */
    fun daysBetween(dateOne: DayOfWeek, dateTwo: DayOfWeek): Int {
        var dateOne: DayOfWeek = dateOne
        var daysInBetween = 0
        while (dateOne !== dateTwo) {
            daysInBetween++
            dateOne = dateOne.plus(1)
        }
        return daysInBetween
    }

    /**
     * Returns the amount of minutes in the given hours and minutes
     *
     * @param hour Number of hours
     * @param minute Number of minutes
     * @return amount of minutes in the given hours and minutes
     */
    fun getPassedMinutesInDay(hour: Int, minute: Int): Int {
        return hour * 60 + minute
    }

    /*
     * Returns the amount of minutes passed in the day before the time in the given date
     * @param date
     * @return amount of minutes in day before time
     */
    fun getPassedMinutesInDay(date: DayTime): Int {
        return getPassedMinutesInDay(date.hour, date.minute)
    }
}
