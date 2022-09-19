package fi.tuni2022.nysselaskin

import java.util.*

const val NIGHT_FARE = 3.0

fun vehicleIcon(vehicle: String): Int {
    if (vehicle == "fi.tuni2022.nysselaskin:id/radioButtonBussi") {
        return R.drawable.ic_baseline_bus_big
    } else if (vehicle == "fi.tuni2022.nysselaskin:id/radioButtonRaitsikka") {
        return R.drawable.ic_baseline_tram_big
    }
    return 0
}

fun calculateSingleTicketPrice(date: Date): Double {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)

    var price = 2.7

    if (hours < 4) {
        price += NIGHT_FARE
    } else if (hours == 4 && minutes <= 40) {
        price += NIGHT_FARE
    }

    return price
}