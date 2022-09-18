package fi.tuni2022.nysselaskin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //auth = Firebase.auth
        updateDatabase()

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewMatkaActivity::class.java)
            resultLauncher.launch(intent)
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
                val zone = "tyhjää"
                if (date != null) {
                    addToDatabase(Matka(date, type, zone))
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
            }
    }
}