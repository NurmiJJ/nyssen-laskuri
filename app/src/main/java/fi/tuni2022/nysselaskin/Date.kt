package fi.tuni2022.nysselaskin

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun timeNow(): Long {
    return System.currentTimeMillis()
}

fun yearInMillis(): Long {
    return 365 * 24 * 60 * 60 * 1000L
}

fun currentHour(): Int {
    val format = SimpleDateFormat("HH", Locale.ENGLISH)
    return format.format(timeNow()).toInt()
}

fun currentMinute(): Int {
    val format = SimpleDateFormat("mm", Locale.ENGLISH)
    return format.format(timeNow()).toInt()
}



fun convertLongToDate(time: Long): String {
    return DateFormat.getDateInstance().format(time)
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