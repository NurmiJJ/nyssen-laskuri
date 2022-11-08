package fi.tuni2022.nysselaskin

import java.util.*
import kotlin.math.roundToInt

const val NIGHT_FARE = 3.0
const val TRANSFER_TIME = 90
const val MONTH = 30
const val YEAR = 360

val CUSTOMERS = mapOf("Aikuinen" to "adult", "Nuori" to "youth", "Lapsi" to "children")

/**
 *  Returns vehicle icon based on vehicle name
 */
fun vehicleIcon(vehicle: String): Int {
    if (vehicle == "Bus") {
        return R.drawable.ic_baseline_bus_big
    } else if (vehicle == "Tram") {
        return R.drawable.ic_baseline_tram_big
    } else if (vehicle == "Train") {
        return R.drawable.ic_baseline_train
    }
    return 0
}

/**
 *  Checks if night fare is needed for the journey
 */
fun isNightFare(date: Date): Boolean {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    var nightFare = false

    if (hours < 4) {
        nightFare = true
    } else if (hours == 4 && minutes <= 40) {
        nightFare = true
    }

    return nightFare
}

/**
 * Checks if new journey is transfer from old journey
 */
fun isTransfer(date: Date, allJourney: ArrayList<Matka>): Boolean {
    for (journey in allJourney) {
        val oldTicketStart = convertDateToLong(journey.date!!)
        val oldTicketEnd = oldTicketStart + TRANSFER_TIME * minutesInMillis()
        val newJourney = convertDateToLong(date)

        if  (newJourney in oldTicketStart..oldTicketEnd){
            // Second journey in same ticket
            return true
        }
    }
    // New ticket
    return false
}

fun getSinglePrice(customer: String, zones: Int): Double {
    val list = TicketPrices.getValue(customer)
    return list[0][zones-2]
}

fun getSeasonPrice(customer: String, zones: Int, duration: Int): Double{
    val list = TicketPrices.getValue(customer)
    return list[seasonDurationToInt(duration)][zones-2]
}

/**
 *  Converts double to have 2 decimals and €
 */
fun convertDoubleToPrice(value: Double): String {
    return String.format("%.2f", value).replace(".", ",") + " €"
}

/**
 * Returns value for access the Ticket Prices database
 */
fun seasonDurationToInt(seasonDuration: Int): Int {
    var value = 0
    
    if(seasonDuration == MONTH) {
        value = 1
    } else if (seasonDuration == YEAR) {
        value = 2
    }
    return value
}

/**
 * Linear estimation for infoDialog
 */
fun estimateTotalTrips(startDate: Date, seasonDuration: Int, currentJourneys: Int): Int{
    val days = daysFromStart(startDate)

    if (days < seasonDuration){
         return (seasonDuration.toDouble() / days.toDouble() * currentJourneys.toDouble()).toInt()

    }
    return currentJourneys
}

fun estimateTotalPayments(startDate: Date, seasonDuration: Int, currentPayments: Double): Double{
    val days = daysFromStart(startDate)

    if (days < seasonDuration){
        return (seasonDuration.toDouble() / days.toDouble() * currentPayments)

    }
    return currentPayments
}

fun neededTrips(customer: String, zones: Int, seasonDuration: Int, currentJourneysPrice: Double): Int{
    val seasonPrice = getSeasonPrice(customer, zones, seasonDuration)
    val singleTicketPrice = getSinglePrice(customer, zones)

    val trips = (seasonPrice - currentJourneysPrice) / singleTicketPrice

    return trips.roundToInt()
}

/**
 * List for each customer groups at order
 * 1. SingleTicket
 * 2. 30 day seasonTicket
 * 3. 360 day seasonTicket
 *
 * Each small list contains zones from left to right 2 zones, 3 zones, 4 ...
 */
    private val adult: List<List<Double>> = listOf(
        listOf(2.10, 3.40, 4.60, 5.90, 7.20),
        listOf(56.0, 73.0, 83.0, 105.0, 115.0),
        listOf(395.0, 555.0, 660.0, 760.0, 860.0))

    private val youth: List<List<Double>> = listOf(
        listOf(1.52, 2.42, 3.30, 4.25, 5.10),
        listOf(39.0, 51.0, 58.0, 73.00, 81.00),
        listOf(280.0, 390.0, 465.0, 540.0, 615.0),
        )

    private val children: List<List<Double>> = listOf(
        listOf(1.05, 1.70, 2.30, 2.95, 3.60),
        listOf(28.0, 36.5, 41.5, 52.5, 57.5),
        listOf(198.0, 287.0, 330.0, 380.0, 430.0),
    )

    private val TicketPrices: Map<String, List<List<Double>>> = mapOf("adult" to adult, "youth" to youth, "children" to children)


