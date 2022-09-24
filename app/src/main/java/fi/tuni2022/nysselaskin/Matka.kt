package fi.tuni2022.nysselaskin

import com.google.firebase.auth.FirebaseUser
import java.util.*

data class Matka(
    val date: Date? = null,
    val vehicleType: String = "",
    val nightFare: Boolean = false,
    val userId: String = ""
)