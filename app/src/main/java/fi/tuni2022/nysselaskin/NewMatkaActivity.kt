package fi.tuni2022.nysselaskin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class NewMatkaActivity : AppCompatActivity() {

    private lateinit var buttonGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_matka)

        val setDate = findViewById<TextView>(R.id.textViewSetDate)
        setDate.text = convertLongToDate(currentTimeToLong())

        val setTime = findViewById<TextView>(R.id.textViewSetTime)
        setTime.text = convertLongToTime(currentTimeToLong())

        buttonGroup = findViewById(R.id.radioGroup)

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        setDate.setOnClickListener {
            datePicker.show(supportFragmentManager, "tag")
        }

        datePicker.addOnPositiveButtonClickListener {
            setDate.text = datePicker.selection?.let { it1 -> convertLongToDate(it1) }
        }

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour())
                .setMinute(currentMinute())
                .setTitleText("Select Appointment time")
                .build()

        setTime.setOnClickListener {
            timePicker.show(supportFragmentManager, "tag");
        }

        timePicker.addOnPositiveButtonClickListener{
            setTime.text = convertIntToTime(timePicker.hour, timePicker.minute)
        }

        val button = findViewById<Button>(R.id.saveButton)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(setDate.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val typeID = buttonGroup.checkedRadioButtonId

                val name = resources.getResourceName(typeID)
                val date = setTime.text.toString() + " " + setDate.text.toString()

                val nightFare = stringToDate(date)?.let { it1 -> isNightFare(it1) }

                val newJourney = "$date;$name;$nightFare"
                replyIntent.putExtra("JOURNEY", newJourney)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }
}