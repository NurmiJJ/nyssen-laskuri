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

        // Set current date and time
        val setDate = findViewById<TextView>(R.id.textInputEditTextDate)
        setDate.text = convertLongToDate(timeNow())

        val setTime = findViewById<TextView>(R.id.textInputEditTextTime)
        setTime.text = convertLongToTime(timeNow())

        buttonGroup = findViewById(R.id.radioGroup)

        // Settings for date picker
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .setStart(yearAgo())
                .setEnd(yearLater())

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.selectDate))
                .setCalendarConstraints(constraintsBuilder.build())
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        // ClickListener for opening date picker
        setDate.setOnClickListener {
            datePicker.show(supportFragmentManager, "tag")
        }

        // ClickListener for showing selected date picker data
        datePicker.addOnPositiveButtonClickListener {
            setDate.text = datePicker.selection?.let { it1 -> convertLongToDate(it1) }
        }

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour())
                .setMinute(currentMinute())
                .setTitleText(getString(R.string.selectTime))
                .build()

        // ClickListener for opening time picker
        setTime.setOnClickListener {
            timePicker.show(supportFragmentManager, "tag")
        }

        // ClickListener for showing selected time picker data
        timePicker.addOnPositiveButtonClickListener{
            setTime.text = convertIntToTime(timePicker.hour, timePicker.minute)
        }

        // Send user data back to MainActivity
        val button = findViewById<Button>(R.id.saveButton)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(setDate.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val typeID = buttonGroup.checkedRadioButtonId

                val name = resources.getResourceName(typeID)
                val split: Array<String> = name.split("radioButton").toTypedArray()
                val type = split[1]
                val date = setTime.text.toString() + " " + setDate.text.toString()

                val nightFare = stringToDate(date)?.let { it1 -> isNightFare(it1) }

                val newJourney = "$date;$type;$nightFare"
                replyIntent.putExtra("JOURNEY", newJourney)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            // Close the activity
            finish()
        }
    }
}