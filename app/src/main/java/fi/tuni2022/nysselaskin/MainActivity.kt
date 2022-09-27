package fi.tuni2022.nysselaskin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private val adapter = MatkaListAdapter(allJourneys, this)

    private lateinit var sharedPreferences: SharedPreferences
    private val model: NysseViewModel by viewModels()

    private lateinit var fab: FloatingActionButton
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var openInNew: TextView

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

    /**
     * Compares two journeys and sorts them order
     * where older is bottom
     */
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

        openInNew = findViewById(R.id.openIcon)

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
                    .setTitle(dateToString(temp.date!!))
                    .setMessage(getString(R.string.deleteMessage))
                    .setIcon(vehicleIcon(temp.vehicleType))
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ -> deleteJourney(temp) }
                    .setNegativeButton(android.R.string.cancel, null).show()
            }
        }

        /**
         * Extra infoView
         */
        val infoView = findViewById<View>(R.id.infoView)
        infoView.setOnClickListener {
            Log.d(TAG, "täälä ollaan")
            Log.d(TAG, adapter.itemCount.toString())
            if (adapter.itemCount != 0) {

                val dialog = AlertDialog.Builder(this)
                    .setTitle(R.string.info)
                    .setView(R.layout.info_dialog)
                    .setPositiveButton(R.string.close, null)
                    .create()

                /**
                 * Here all data from added journeys are set to infoView before
                 * it is displayed to user
                 */
                dialog.setOnShowListener{
                    val startDate = allJourneys[allJourneys.size-1].date!!
                    val seasonDuration = sharedPreferences.getString("duration", "").toString().toInt()

                    val setStartDate = dialog.findViewById<TextView>(R.id.textInputStartDate)
                    setStartDate.text = onlyDateToString(startDate)

                    val setEndDate = dialog.findViewById<TextView>(R.id.textInputEndDate)
                    setEndDate.text = onlyDateToString(lastDay(startDate, seasonDuration))

                    val setEstimate = dialog.findViewById<TextView>(R.id.textInputJourneyEstimate)
                    setEstimate.text = estimateTotalTrips(startDate, seasonDuration,
                                       adapter.itemCount).toString()

                    val setDaysLeft = dialog.findViewById<TextView>(R.id.textInputDaysLeft)
                    setDaysLeft.text = daysLeft(startDate, seasonDuration).toString()

                }
                dialog.show()
            }
        }

        /**
         * Menu at top bar
         */
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
        checkUserStatus()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Function that solves result of addNewActivity
     * Creates a new journey if result ok
     */
    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
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

    /**
     * Adds new journey to Firebase database
     */
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

    /**
     * Calculates current price of all journeys and
     * displays it in UI
     */
    private fun updatePrice() {
        var totalPrice = 0.0
        val customer = CUSTOMERS[sharedPreferences.getString("customer", "")].toString()
        val zone = sharedPreferences.getString("zones", "").toString().toInt()
        val singlePrice = getSinglePrice(customer, zone)

        for (journey in allJourneys){
            totalPrice += singlePrice

            // If there is night fare it must be remembered here
            if (journey.nightFare){
                totalPrice += NIGHT_FARE
            }
        }

        totalPriceView.text = convertDoubleToPrice(totalPrice)
    }


    @SuppressLint("NotifyDataSetChanged")
    /**
     * Listener for Firebase database
     * Keeps users journeys in arrayList for RecyclerView
     */
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
                // Must update the UI fields after update
                Collections.sort(allJourneys, Compare())
                adapter.notifyDataSetChanged()
                totalView.text = adapter.itemCount.toString()
                updatePrice()
                startScreen()
            }
    }

    /**
     * Deletes given journey from Firebase Database
     * Because validation is done by journey's date
     * and date won't have seconds, this function will
     * delete all journeys at that time
     */
    private fun deleteJourney(matka: Matka){
        database.collection(collection)
            .whereEqualTo("userId", auth.currentUser!!.uid)
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

    /**
     * Deletes users whole database
     */
    private fun deleteDatabase() {
        val user = auth.currentUser!!.uid
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.deleteDatabase))
            .setMessage(getString(R.string.deleteDatabaseMessage))
            .setIcon(android.R.drawable.ic_menu_delete)
            .setPositiveButton(android.R.string.ok)
            { _, _ ->
                database.collection(collection)
                    .whereEqualTo("userId", user)
                    .get()
                    .addOnSuccessListener { value ->
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
            }
            .setNegativeButton(android.R.string.cancel, null).show()

    }

    /**
     * Shows welcome message if there is any journeys
     * in Firebase database
     */
    private fun startScreen(){
        if (adapter.itemCount == 0) {
            nightFareTitle.visibility = TextView.INVISIBLE
            journeyDateTitle.visibility = TextView.INVISIBLE
            openInNew.visibility = TextView.INVISIBLE

            helloView.visibility = TextView.VISIBLE
        } else {
            nightFareTitle.visibility = TextView.VISIBLE
            journeyDateTitle.visibility = TextView.VISIBLE
            openInNew.visibility = TextView.VISIBLE

            helloView.visibility = TextView.INVISIBLE
        }
    }

    /**
     * Calculates needed journeys at season to have season
     * ticket cheaper than single tickets
     * Updates that value to UI
     */
    private fun updateNeededJourneys(){
        val customerType = CUSTOMERS[sharedPreferences.getString("customer", "")].toString()
        val seasonDuration = sharedPreferences.getString("duration", "").toString().toInt()
        val zones = sharedPreferences.getString("zones", "").toString().toInt()

        val seasonPrice = getSeasonPrice(customerType, zones, seasonDuration)
        val singleTicketPrice = getSinglePrice(customerType, zones)

        val ticket = (seasonPrice / singleTicketPrice).roundToInt()

        neededJourneysView.text = ticket.toString()
    }

    /**
     * Updates UI if there is update at settings
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val text = sharedPreferences.getString(key, "").toString()
        updateNeededJourneys()
        updatePrice()

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

    /**
     * Launch signIn intent or logs user out depending status
     * of current user
     */
    private fun singInOut(){
        if (auth.currentUser != null) {
            logOut()

        } else {
            signInLauncher.launch(signInIntent)
        }
    }

    /**
     * Logs current user out from Firebase database
     */
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
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    /**
     * Changes UI depending status of current user
     */
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

    // All provided ways to log in the Firebase database
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.AnonymousBuilder().build())

    // Create and launch sign-in intent
    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()
}