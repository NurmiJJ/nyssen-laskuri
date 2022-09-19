package fi.tuni2022.nysselaskin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

const val TAG = "Nyssesofta"
const val collection : String = "Journeys"

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = Firebase.firestore

    private var allJourneys: ArrayList<Matka> = ArrayList()
    private val adapter = MatkaListAdapter(allJourneys)

    private lateinit var total: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //auth = Firebase.auth
        updateDatabase()
        total = findViewById(R.id.textViewTotal)

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
                val zone = split[2]
                val price = split[3].toDouble()
                if (date != null) {
                    addToDatabase(Matka(date, type, zone, price))
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
                total.text = allJourneys.size.toString()
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
}