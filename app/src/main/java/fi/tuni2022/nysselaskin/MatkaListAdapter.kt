package fi.tuni2022.nysselaskin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MatkaListAdapter(private val mList: ArrayList<Matka> ): ListAdapter<Matka, MatkaListAdapter.MatkaViewHolder>(MatkaComparator()) {

    private var matka: Matka? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatkaViewHolder {
        return MatkaViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: MatkaViewHolder, position: Int) {
        val current = mList[position]
        holder.bind(current)

        holder.itemView.setOnClickListener { v ->
            matka = current

            Snackbar.make(v, "Matka " + current.date, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

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

    fun getJourney(): Matka? {
        return matka
    }

    override fun getItemCount() = mList.size

    class MatkaComparator : DiffUtil.ItemCallback<Matka>() {
        override fun areItemsTheSame(oldItem: Matka, newItem: Matka): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Matka, newItem: Matka): Boolean {
            return oldItem.date == newItem.date
        }
    }
}