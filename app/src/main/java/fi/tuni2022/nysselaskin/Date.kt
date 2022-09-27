package fi.tuni2022.nysselaskin

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun timeNow(): Long {
    return System.currentTimeMillis()
}

fun yearInMillis(): Long {
    return 365 * dayInMillis()
}

fun dayInMillis(): Long {
    return 24 * 60 * 60 * 1000L
}

fun currentHour(): Int {
    val format = SimpleDateFormat("HH", Locale.ENGLISH)
    return format.format(timeNow()).toInt()
}

fun currentMinute(): Int {
    val format = SimpleDateFormat("mm", Locale.ENGLISH)
    return format.format(timeNow()).toInt()
}

fun daysFromStart(date: Date): Int {
    val today = timeNow()
    val difference = today - convertDateToLong(date)
    return TimeUnit.MILLISECONDS.toDays(difference).toInt() + 1
}

/**
 * Returns [daysLeft] at current season
 * if season already ended then returns 0
 */
fun daysLeft(startDate: Date, seasonDuration: Int): Int {
    val days = daysFromStart(startDate)
    var daysLeft = 0
    if (seasonDuration - days > 0){
        daysLeft = seasonDuration - days
    }
    return daysLeft
}

/**
 * Return the seasons last day
 */
fun lastDay(startDate: Date, seasonDuration: Int): Date {
    val startDateLong = convertDateToLong(startDate)
    val lastDay = startDateLong + (dayInMillis() * seasonDuration)
    return Date(lastDay)
}


fun convertLongToDate(time: Long): String {
    return DateFormat.getDateInstance().format(time)
}

fun convertDateToLong(date: Date): Long {
    return date.time
}

fun convertLongToTime(time: Long): String{
    val date = Date(time)
    val format = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    return format.format(date)
}

fun convertIntToTime(hour: Int, minute: Int): String {
    return "$hour:$minute"
}



fun stringToDate(date: String): Date? {
    val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH)
    return format.parse(date)
}

fun dateToString(date: Date): String {
    val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH)
    return format.format(date)
}

fun onlyDateToString(date: Date): String {
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
    return format.format(date)
}


// For limiting date picker
fun yearAgo(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = timeNow() - yearInMillis()
    calendar[Calendar.MONTH] = Calendar.JANUARY
    return calendar.timeInMillis
}

// For limiting date picker
fun yearLater(): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = timeNow() + yearInMillis()
    calendar[Calendar.MONTH] = Calendar.JANUARY
    return calendar.timeInMillis
}