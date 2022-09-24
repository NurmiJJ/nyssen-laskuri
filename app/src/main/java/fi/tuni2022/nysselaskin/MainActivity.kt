package fi.tuni2022.nysselaskin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt


const val TAG = "Nyssesofta"
const val collection : String = "Journeys"

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{

    private lateinit var auth: FirebaseAuth
    private val database = Firebase.firestore

    private var allJourneys: ArrayList<Matka> = ArrayList()
    private val adapter = MatkaListAdapter(allJourneys)

    private lateinit var sharedPreferences: SharedPreferences
    private val model: NysseViewModel by viewModels()

    private lateinit var totalView: TextView
    private lateinit var totalPriceView: TextView
    private lateinit var neededJourneysView: TextView

    private lateinit var customerView: TextView
    private lateinit var durationView: TextView
    private lateinit var zoneView: TextView

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
            onSharedPreferenceChanged(pref, key)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        //auth = Firebase.auth
        updateDatabase()
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

        val fab = findViewById<FloatingActionButton>(R.id.fab)
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

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    Log.d(TAG, "Settings painettu")
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
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
                    addToDatabase(Matka(date, type, nightFare))
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
                adapter.notifyDataSetChanged()
                totalView.text = allJourneys.size.toString()
                updatePrice()
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
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
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

}