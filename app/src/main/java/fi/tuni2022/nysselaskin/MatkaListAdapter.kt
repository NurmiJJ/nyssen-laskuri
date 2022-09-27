package fi.tuni2022.nysselaskin

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


/**
 * Creates MatkaViewHolder and binds it in onBindViewHolder
 */
class MatkaListAdapter(private val mList: ArrayList<Matka>, private val context: Context): ListAdapter<Matka, MatkaListAdapter.MatkaViewHolder>(MatkaComparator()) {

    private var matka: Matka? = null
    private var toastMessage: Toast? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkaViewHolder {
        return MatkaViewHolder.create(parent)
    }

    /**
     * Creates possibility to click recyclerView item
     */
    override fun onBindViewHolder(holder: MatkaViewHolder, position: Int) {
        val current = mList[position]
        holder.bind(current)

        holder.itemView.setOnClickListener {
            toastMessage?.cancel()
            matka = current

            val str = context.getString(R.string.chooseMessage) +
                    " " +  dateToString(current.date!!)

            val toastMessage = Toast.makeText(context,
                str, Toast.LENGTH_SHORT
            )

            toastMessage.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
            toastMessage.show()

            // A little vibration for indicate the click
            val vibrator = getSystemService(context, Vibrator::class.java)
            vibrator?.vibrate((VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)))


        }
    }

    /**
     * Binds journey data to one recyclerView line
     */
    class MatkaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeItemView: TextView = itemView.findViewById(R.id.textViewType)
        private val journeyItemView: TextView = itemView.findViewById(R.id.textViewJourneyDate)
        private val nightFareItemView: TextView = itemView.findViewById(R.id.textViewNightFare)

        fun bind(matka: Matka) {
            typeItemView.setCompoundDrawablesWithIntrinsicBounds(vehicleIcon(matka.vehicleType), 0, 0, 0)
            journeyItemView.text = matka.date?.let { dateToString(it) }
            if (matka.nightFare) {
                nightFareItemView.text = "X"
            }
        }

        companion object {
            fun create(parent: ViewGroup): MatkaViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return MatkaViewHolder(view)
            }
        }
    }

    /**
     * Returns selected journey for single deletion
     */
    fun getJourney(): Matka? {
        return matka
    }

    /**
     * To make impossible to delete single journey twice
     */
    fun journeyDeleted(){
        matka = null
    }

    override fun getItemCount() = mList.size

    /**
     * Defines how to compute if two words are the same
     * or if the contents are the same.
     */
    class MatkaComparator : DiffUtil.ItemCallback<Matka>() {
        override fun areItemsTheSame(oldItem: Matka, newItem: Matka): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Matka, newItem: Matka): Boolean {
            return oldItem.date == newItem.date
        }
    }
}