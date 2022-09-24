package fi.tuni2022.nysselaskin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.math.roundToInt


const val TAG = "Nyssesofta"
const val collection : String = "Journeys"

class MainActivity : AppCompatActivity(),   SharedPreferences.OnSharedPreferenceChangeListener{

    private val database = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    private var allJourneys: ArrayList<Matka> = ArrayList()
    private val adapter = MatkaListAdapter(allJourneys)

    private lateinit var sharedPreferences: SharedPreferences
    private val model: NysseViewModel by viewModels()

    private lateinit var fab: FloatingActionButton
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var totalView: TextView
    private lateinit var totalPriceView: TextView
    private lateinit var neededJourneysView: TextView

    private lateinit var customerView: TextView
    private lateinit var durationView: TextView
    private lateinit var zoneView: TextView

    private lateinit var journeyDateTitle: TextView
    private lateinit var nightFareTitle: TextView
    private lateinit var helloView: TextView


    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            onSharedPreferenceChanged(pref, key)
        }

    class Compare: Comparator<Matka>{
        override fun compare(p0: Matka, p1: Matka): Int {
            return p1.date!!.compareTo(p0.date)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        totalView = findViewById(R.id.textViewTotal)
        totalPriceView = findViewById(R.id.textViewTotalPrice)
        neededJourneysView = findViewById(R.id.textViewNeededJourneys)

        durationView = findViewById(R.id.textViewDuration)
        customerView = findViewById(R.id.textViewCustomer)
        zoneView = findViewById(R.id.textViewZones)

        journeyDateTitle = findViewById(R.id.textViewJourneyDateTitle)
        nightFareTitle = findViewById(R.id.textViewFareTitle)
        helloView = findViewById(R.id.textViewHello)

        updateNeededJourneys()

        val customerObserver = Observer<String> { newCustomer ->
            customerView.text = newCustomer
        }

        val durationObserver = Observer<String> { newTicketDuration ->
            durationView.text = newTicketDuration.toString()
        }

        val zoneObserver = Observer<String> { newZone ->
            zoneView.text = newZone.toString()
        }

        model.customer.observe(this, customerObserver)
        model.seasonDuration.observe(this, durationObserver)
        model.zones.observe(this, zoneObserver)

        model.customer.value = sharedPreferences.getString("customer", "")
        model.seasonDuration.value = sharedPreferences.getString("duration", "")
        model.zones.value = sharedPreferences.getString("zones", "")

        fab = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMatkaActivity::class.java)
            resultLauncher.launch(intent)
        }

        val buttonDeleteOne = findViewById<Button>(R.id.buttonDeleteOne)
        buttonDeleteOne.setOnClickListener {
            val temp: Matka? = adapter.getJourney()
            if (temp != null) {
                Log.d(TAG, temp.date.toString())
                AlertDialog.Builder(this)
                    .setTitle("Delete Journey")
                    .setMessage("Do you really want to delete " + temp.date + " ?")
                    .setIcon(android.R.drawable.ic_delete)
                    .setPositiveButton(android.R.string.yes,
                        DialogInterface.OnClickListener { dialog, whichButton ->
                            deleteJourney(temp)
                        })
                    .setNegativeButton(android.R.string.no, null).show()
            }
        }

        topAppBar = findViewById(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.Log -> {
                    singInOut()
                    true
                }
                R.id.delete -> {
                    deleteDatabase()
                    true
                }

                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            Log.d(TAG, "tässä ja nyt")
            updateDatabase()
        }
        //checkUserStatus()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "päästiin ulos")
            // There are no request codes
            val data: Intent? = result.data
            if (data != null) {
                val split: Array<String> = data.getStringExtra("JOURNEY")
                    .toString().split(";").toTypedArray()


                Log.d(TAG, split[0])
                val date = stringToDate(split[0])
                val type = split[1]
                val nightFare = split[2].toBoolean()
                if (date != null) {
                    addToDatabase(Matka(date, type, nightFare, auth.currentUser!!.uid))
                }
            }
        }
    }

    private fun addToDatabase(matka: Matka){
        // Add a new document with a generated ID
        database.collection(collection)
            .add(matka)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePrice() {
        var totalPrice = 0.0
        val customer = CUSTOMERS[sharedPreferences.getString("customer", "")].toString()
        val zone = sharedPreferences.getString("zones", "").toString().toInt()
        val singlePrice = getSinglePrice(customer, zone)

        Log.d(TAG, totalPrice.toString())
        for (journey in allJourneys){
            totalPrice += singlePrice
            if (journey.nightFare){
                totalPrice += NIGHT_FARE
            }
            Log.d(TAG, totalPrice.toString())
        }

        totalPriceView.text = convertDoubleToPrice(totalPrice)
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateDatabase() {
        database.collection(collection)
            .whereEqualTo("userId", auth.currentUser!!.uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                allJourneys.clear()
                for (doc in value!!) {
                    val matka: Matka = doc.toObject(Matka::class.java)
                    allJourneys.add(matka)
                    Log.w(TAG, matka.toString())

                }
                Collections.sort(allJourneys, Compare())
                adapter.notifyDataSetChanged()
                totalView.text = allJourneys.size.toString()
                updatePrice()
                startScreen()
            }
    }

    private fun deleteJourney(matka: Matka){
        database.collection(collection)
            .whereEqualTo("date", matka.date)
            .get()
            .addOnSuccessListener {value ->
                if (value != null) {
                    for (doc in value) {
                        doc.reference.delete()
                    }
                }
                adapter.journeyDeleted()
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun deleteDatabase() {
        val user = auth.currentUser!!.uid
        AlertDialog.Builder(this)
            .setTitle("Delete Journey")
            .setMessage("Do you really want to delete your database ?")
            .setIcon(android.R.drawable.ic_delete)
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, whichButton ->
                    database.collection(collection)
                        .whereEqualTo("userId", user)
                        .get()
                        .addOnSuccessListener {value ->
                            if (value != null) {
                                for (doc in value) {
                                    doc.reference.delete()
                                }
                                adapter.journeyDeleted()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }
                })
            .setNegativeButton(android.R.string.no, null).show()

    }

    private fun startScreen(){
        if (adapter.itemCount == 0) {
            nightFareTitle.visibility = TextView.INVISIBLE
            journeyDateTitle.visibility = TextView.INVISIBLE

            helloView.visibility = TextView.VISIBLE
        } else {
            nightFareTitle.visibility = TextView.VISIBLE
            journeyDateTitle.visibility = TextView.VISIBLE

            helloView.visibility = TextView.INVISIBLE
        }
    }

    private fun updateNeededJourneys(){
        val customerType = CUSTOMERS[sharedPreferences.getString("customer", "")].toString()
        val seasonDuration = sharedPreferences.getString("duration", "").toString().toInt()
        val zones = sharedPreferences.getString("zones", "").toString().toInt()

        val seasonPrice = getSeasonPrice(customerType, zones, seasonDuration)
        val singleTicketPrice = getSinglePrice(customerType, zones)

        val ticket = (seasonPrice / singleTicketPrice).roundToInt()

        neededJourneysView.text = ticket.toString()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val text = sharedPreferences.getString(key, "").toString()
        updateNeededJourneys()
        updatePrice()

        if (key == getString(R.string.deleteDatabase)) {
            Log.d(TAG, "Nappia painettu")
        }
        if (key == "customer") {
            model.customer.value = text
        }
        if (key == "duration") {
            model.seasonDuration.value = text
        }
        if (key == "zones") {
            model.zones.value = text
        }

    }

    private fun singInOut(){
        if (auth.currentUser != null) {
            logOut()

        } else {
            signInLauncher.launch(signInIntent)
        }
    }

    private fun logOut(){
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val num = adapter.itemCount
                allJourneys.clear()
                adapter.notifyItemRangeRemoved(0, num)

                checkUserStatus()
                startScreen()
                updateNeededJourneys()
                updatePrice()

                Log.d(TAG, "Uloskirjautuminen onnistui!")
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun checkUserStatus(){
        if(auth.currentUser != null){
            helloView.text = getString(R.string.hello)
            fab.isEnabled = true
            topAppBar.menu.findItem(R.id.Log).title = getString(R.string.LogOut)
            updateDatabase()
        } else {
            helloView.text = getString(R.string.noUser)
            fab.isEnabled = false
            topAppBar.menu.findItem(R.id.Log).title = getString(R.string.LogIn)
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                checkUserStatus()

            }
        }
    }

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.AnonymousBuilder().build())

    // Create and launch sign-in intent
    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()
}