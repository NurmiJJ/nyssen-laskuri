package fi.tuni2022.nysselaskin

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

fun currentTimeToLong(): Long {
    return System.currentTimeMillis()
}

fun currentHour(): Int {
    val time = Date(System.currentTimeMillis())
    val format = SimpleDateFormat("HH", Locale.ENGLISH)

    return format.format(time).toInt()
}

fun currentMinute(): Int {
    val time = Date(System.currentTimeMillis())
    val format = SimpleDateFormat("mm", Locale.ENGLISH)

    return format.format(time).toInt()
}

fun stringToDate(date: String): Date? {
    val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH)
    return format.parse(date)
}

fun dateToString(date: Date): String {
    val format = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH)
    return format.format(date)
}