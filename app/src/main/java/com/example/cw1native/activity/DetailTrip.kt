package com.example.cw1native.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cw1native.R
import com.example.cw1native.adapter.ExpenseAdapter
import com.example.cw1native.models.Expense
import com.example.cw1native.models.ShowTrip
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DetailTrip : AppCompatActivity() {

    private lateinit var database : DatabaseReference
    private lateinit var riskValue: String
    private lateinit var tripNameParent : String
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var longitude: String
    private lateinit var  latitude: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_trip)

        //Connect database
        database = FirebaseDatabase.getInstance("https://cw1-native-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Trips")

        //Pass data into activity
        val trip = intent.getParcelableExtra<ShowTrip>("trip")
        if(trip?.tripName!!.isNotEmpty()) {
            val tripName : TextView = findViewById(R.id.tripNameInfo)
            val destination : TextView = findViewById(R.id.destinationInfo)
            val date : TextView = findViewById(R.id.dateInfo)
            val riskAssessment : TextView = findViewById(R.id.riskAssessmentInfo)
            val description : TextView = findViewById(R.id.descriptionInfo)
            val phone: TextView = findViewById(R.id.phoneInfo)
            val country : TextView = findViewById(R.id.countryInfo)

            tripNameParent = trip.tripName

            readData(trip.tripName, tripName, destination, date, riskAssessment, description, phone,country)

            showExpenses(LinearLayoutManager(this))
        }

        //Update Dialog
        val updateButton: MaterialButton = findViewById(R.id.updateButton)

        updateButton.setOnClickListener {
            updateTrip(trip.tripName)
        }

        //Delete Dialog
        val deleteButton: MaterialButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            deleteTrip(trip.tripName)
        }

        //Add Expense Dialog
        val addButton : MaterialButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            addExpense()
        }

        //Show On Map
        findViewById<ImageView>(R.id.copyToClipboard).setOnClickListener {

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Google Map", "https://maps.google.com/?q=$latitude,$longitude")
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readData(tripName: String, tripNameView: TextView, destinationView: TextView,
        dateView: TextView, riskAssessmentView: TextView, descriptionView: TextView,
        phoneView: TextView, countryView: TextView) {
        database.child(tripName).get().addOnSuccessListener {

            if (it.exists()){

                val destination = it.child("destination").value
                val date = it.child("date").value
                val riskAssessment = it.child("riskAssessment").value
                val description = it.child("description").value
                val phone = it.child("phone").value
                val country = it.child("country").value

                latitude = it.child("latitude").value.toString()
                longitude = it.child("longitude").value.toString()

                tripNameView.text = tripName
                destinationView.text = destination.toString()
                dateView.text = date.toString()
                riskAssessmentView.text = riskAssessment.toString()
                descriptionView.text = description.toString()
                phoneView.text = phone.toString()
                countryView.text = country.toString()

                riskValue = riskAssessment.toString()

                findViewById<TextView>(R.id.showOnMap).text = "https://maps.google.com/?q=$latitude,$longitude"
            }
        }

    }

    private fun deleteTrip(tripName: String) {
        val message = "Confirm to delete"
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                database.child(tripName).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Successfully Deleted", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateLable(myCalendar: Calendar, dateInput: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        dateInput.setText(sdf.format(myCalendar.time))
    }

    private fun updateTrip(trip: String?) {
        val updateDialogLayout = LayoutInflater.from(this).inflate(R.layout.update_trip_dialog, null)

        //Set to AutoCompleteTextView
        val countriesList = arrayOf("Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Côte d'Ivoire", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo (Congo-Brazzaville)", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czechia (Czech Republic)", "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Eswatini (fmr. \"Swaziland\")", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Holy See", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar (formerly Burma)", "Namibia", "Nauru", "Netherlands", "New Zealand", "Nicaragua", "Niger", "North Korea", "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Palestine State", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States of America", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countriesList)
        updateDialogLayout.findViewById<AutoCompleteTextView>(R.id.countryEditText).setAdapter(adapter)

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar, updateDialogLayout.findViewById(R.id.dateEditText))
        }

        updateDialogLayout.findViewById<TextInputEditText>(R.id.dateEditText).setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        if(trip!!.isNotEmpty()) {
            val destination : TextInputLayout = updateDialogLayout.findViewById(R.id.destinationContainer)
            val date : TextInputLayout = updateDialogLayout.findViewById(R.id.dateContainer)
            val riskAssessment : SwitchCompat = updateDialogLayout.findViewById(R.id.requiredRisk)
            val description : TextInputLayout = updateDialogLayout.findViewById(R.id.descriptionContainer)
            val phone: TextInputLayout = updateDialogLayout.findViewById(R.id.phoneContainer)
            val country : AutoCompleteTextView = updateDialogLayout.findViewById(R.id.countryEditText)

            setTextInput(trip, destination, date, riskAssessment, description, phone,country)
        }

        val assessment : SwitchCompat = updateDialogLayout.findViewById(R.id.requiredRisk)
        assessment.setOnCheckedChangeListener { _, isChecked ->
            val value = if(isChecked)
                "Yes"
            else
                "No"
            if(riskValue != value) {
                setRiskValue(value)
            }
        }

        //Create dialog
        val searchDialog = AlertDialog.Builder(this)
        searchDialog.setTitle("Update Trip")
        searchDialog.setView(updateDialogLayout)
        searchDialog.setPositiveButton("Update") { _, _ ->
            val destination : TextInputEditText = updateDialogLayout.findViewById(R.id.destinationEditText)
            val date : TextInputEditText = updateDialogLayout.findViewById(R.id.dateEditText)
            val description : TextInputEditText = updateDialogLayout.findViewById(R.id.descriptionEditText)
            val phone : TextInputEditText = updateDialogLayout.findViewById(R.id.phoneEditText)
            val country : AutoCompleteTextView = updateDialogLayout.findViewById(R.id.countryEditText)

            val tripUpdate = mapOf(
                "destination" to destination.text.toString(),
                "date" to date.text.toString(),
                "description" to description.text.toString(),
                "phone" to phone.text.toString(),
                "country" to country.text.toString(),
                "riskAssessment" to riskValue
            )

            database.child(tripNameParent).updateChildren(tripUpdate).addOnSuccessListener {
                Toast.makeText(this, "Successfully Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
        searchDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()

        }
        searchDialog.show()
    }

    private fun setRiskValue(value: String) {
        riskValue = value
    }

    private fun setTextInput(tripName: String, destinationView: TextInputLayout,
         dateView: TextInputLayout, riskAssessmentView: SwitchCompat, descriptionView: TextInputLayout,
         phoneView: TextInputLayout, countryView: AutoCompleteTextView) {

        database.child(tripName).get().addOnSuccessListener {

            if (it.exists()){

                val destination = it.child("destination").value
                val date = it.child("date").value
                val riskAssessment = it.child("riskAssessment").value
                val description = it.child("description").value
                val phone = it.child("phone").value
                val country = it.child("country").value

                destinationView.editText?.setText(destination.toString())
                dateView.editText?.setText(date.toString())
                descriptionView.editText?.setText(description.toString())
                phoneView.editText?.setText(phone.toString())
                countryView.setText(country.toString())

                if(riskAssessment == "Yes") {
                    riskAssessmentView.isChecked = true
                }

            }
        }
    }

    private fun inputFocusListener(expenseTypeView: AutoCompleteTextView,
        amountView: TextInputEditText, dateView: TextInputEditText, timeView: TextInputEditText, dialog: View
    ) {

        //Expense Type
        expenseTypeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.typeContainer).helperText = validExpenseType(dialog)
            }
        }

        //Amount
        amountView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.typeContainer).helperText = validAmount(dialog)
            }
        }

        //Date
        dateView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.typeContainer).helperText = validDate(dialog)
            }
        }

        //Time
        timeView.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                dialog.findViewById<TextInputLayout>(R.id.typeContainer).helperText = validTime(dialog)
            }
        }

    }

    private fun validExpenseType(dialog: View): String? {
        val expenseType = dialog.findViewById<AutoCompleteTextView>(R.id.typeEditText).text.toString()
        if(expenseType.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validAmount(dialog: View): String? {
        val amount = dialog.findViewById<TextInputEditText>(R.id.amountEditText).text.toString()
        if(amount.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validDate(dialog: View): String? {
        val date = dialog.findViewById<TextInputEditText>(R.id.dateEditText).text.toString()
        if(date.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validTime(dialog: View): String? {
        val time = dialog.findViewById<TextInputEditText>(R.id.timeEditText).text.toString()
        if(time.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun submitForm(dialog: View)
    {
        val expenseTypeView : TextInputLayout = dialog.findViewById(R.id.typeContainer)
        val amountView : TextInputLayout = dialog.findViewById(R.id.amountContainer)
        val dateView : TextInputLayout= dialog.findViewById(R.id.dateContainer)
        val timeView : TextInputLayout= dialog.findViewById(R.id.timeContainer)

        expenseTypeView.helperText = validExpenseType(dialog)
        amountView.helperText = validAmount(dialog)
        dateView.helperText = validDate(dialog)
        timeView.helperText = validTime(dialog)

        val validExpenseType = expenseTypeView.helperText === null
        val validAmount = amountView.helperText === null
        val validDate = dateView.helperText === null
        val validTime = timeView.helperText === null

        if (validExpenseType && validAmount && validDate && validTime) {
            return saveData(
                dialog.findViewById<AutoCompleteTextView>(R.id.typeEditText).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.amountEditText).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.dateEditText).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.timeEditText).text.toString(),
                dialog.findViewById<TextInputEditText>(R.id.commentEditText).text.toString(),
                dialog)
        }
        return

    }

    private fun saveData(
        expenseType: String,
        amount: String,
        date: String,
        time: String,
        comment: String,
        dialog: View
    ) {
        val expense = Expense(
            expenseType,
            amount,
            date,
            time,
            comment
        )
        database.child(tripNameParent).child("expenses").push().setValue(expense).addOnSuccessListener {
            dialog.findViewById<AutoCompleteTextView>(R.id.typeEditText).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.amountEditText).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.dateEditText).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.timeEditText).text?.clear()
            dialog.findViewById<TextInputEditText>(R.id.commentEditText).text?.clear()

            Toast.makeText(this, "Successfully Added", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpense() {
        val addDialogLayout = LayoutInflater.from(this).inflate(R.layout.add_expense_dialog, null)

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar, addDialogLayout.findViewById(R.id.dateEditText))
        }

       addDialogLayout.findViewById<TextInputEditText>(R.id.dateEditText).setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Time Picker
        val timeInput : TextInputEditText = addDialogLayout.findViewById(R.id.timeEditText)
        timeInput.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val startHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentTime.get(Calendar.MINUTE)

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                timeInput.setText("$hourOfDay:$minute")
            }, startHour, startMinute, false).show()

        }

        //Dropdown Menu
        val items = listOf("Travel", "Food", "Other")
        val adapter = ArrayAdapter(this, R.layout.list_type_expense ,items)
        addDialogLayout.findViewById<AutoCompleteTextView>(R.id.typeEditText).setAdapter(adapter)

        //Create dialog
        val addExpenseDialog = AlertDialog.Builder(this)
        addExpenseDialog.setView(addDialogLayout)
        addExpenseDialog.show()

        //Validate
        inputFocusListener(
            addDialogLayout.findViewById(R.id.typeEditText),
            addDialogLayout.findViewById(R.id.amountEditText),
            addDialogLayout.findViewById(R.id.dateEditText),
            addDialogLayout.findViewById(R.id.timeEditText),
            addDialogLayout
        )

        addDialogLayout.findViewById<MaterialButton>(R.id.submitButton).setOnClickListener {
            submitForm(addDialogLayout)
        }

    }

    private fun showExpenses(layout: LinearLayoutManager) {
        database.child(tripNameParent).child("expenses").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {

                    val _expenseList : List<Expense> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(Expense::class.java)!!

                    }

                    expenseRecyclerView = findViewById(R.id.listViewExpense)
                    expenseRecyclerView.layoutManager = layout
                    expenseRecyclerView.setHasFixedSize(true)
                    adapter = ExpenseAdapter()
                    expenseRecyclerView.adapter = adapter

                    adapter.updateExpenseList(_expenseList)

                }catch (e : Exception){
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}