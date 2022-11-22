package com.example.cw1native.fragment

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.cw1native.databinding.FragmentAddBinding
import com.example.cw1native.models.AddTrip
import com.example.cw1native.models.ShowTrip
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Add : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private var riskValue: String = "No"
    private var latitue: Number = 0
    private var longitude: Number = 0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var database : DatabaseReference
    private lateinit var tripNameList : kotlin.collections.List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocation()

        //Connect database
        database = FirebaseDatabase.getInstance("https://cw1-native-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Trips")

        //Make TripNameLis
        queryTrips()

        //Date Picker
        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLable(myCalendar, binding)
        }

        binding.dateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //Set to AutoCompleteTextView
        val countriesList = arrayOf("Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Côte d'Ivoire", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo (Congo-Brazzaville)", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czechia (Czech Republic)", "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Eswatini (fmr. \"Swaziland\")", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Holy See", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar (formerly Burma)", "Namibia", "Nauru", "Netherlands", "New Zealand", "Nicaragua", "Niger", "North Korea", "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Palestine State", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States of America", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, countriesList)
        binding.countryEditText.setAdapter(adapter)

        //Validate
        inputFocusListener(binding)
        binding.submitButton.setOnClickListener {
            submitForm(binding)
        }

        binding.requiredRisk.setOnCheckedChangeListener { _, isChecked ->
            val value = if (isChecked)
                "Yes"
            else
                "No"
            setRiskValue(value)
        }

        binding.resetButton.setOnClickListener {
            resetTripData()
        }

        return binding.root
    }

    private fun updateLable(myCalendar: Calendar, binding: FragmentAddBinding) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.dateEditText.setText(sdf.format(myCalendar.time))
    }

    private fun submitForm(binding: FragmentAddBinding)
    {
        binding.tripNameContainer.helperText = validTripName(binding)
        binding.destinationContainer.helperText = validDestination(binding)
        binding.dateContainer.helperText = validDate(binding)
        binding.countryContainer.helperText = validCountry(binding)

        val validTripName = binding.tripNameContainer.helperText === null
        val validDestination = binding.destinationContainer.helperText === null
        val validDate = binding.dateContainer.helperText === null
        val validCountry = binding.countryContainer.helperText === null

        if (validTripName && validDestination && validDate && validCountry) {
            return resetForm(binding)
        }
        return

    }

    private fun setRiskValue(value: String) {
        riskValue = value
    }

    private fun resetForm(binding: FragmentAddBinding)
    {
        var message = "Trip Name:               ==>  " + binding.tripNameEditText.text
        message += "\nDestination:             ==>  " + binding.destinationEditText.text
        message += "\nDate:                         ==>  " + binding.dateEditText.text
        message += "\nDescription:             ==>  " + binding.descriptionEditText.text
        message += "\nPhone Number:       ==>  " + binding.phoneEditText.text
        message += "\nCountry:                    ==>  " + binding.countryEditText.text
        message += "\nRisk Assessment:   ==>  $riskValue "
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                saveData(
                    binding.tripNameEditText.text.toString(),
                    binding.destinationEditText.text.toString(),
                    binding.dateEditText.text.toString(),
                    binding.countryEditText.text.toString(),
                    riskValue,
                    latitue,
                    longitude,
                    binding.descriptionEditText.text.toString(),
                    binding.phoneEditText.text.toString(),
                    binding
                )
            }
            .setNegativeButton("Edit") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveData(
        tripName: String,
        destination: String,
        date: String,
        country: String,
        riskAssessment: String,
        latitude: Number,
        longitude: Number,
        description: String?,
        phone: String?,
        binding: FragmentAddBinding
    ) {
        val trip = AddTrip(
            tripName,
            destination,
            date,
            description,
            phone,
            country,
            riskAssessment,
            latitude,
            longitude,
        )
        database.child(tripName).setValue(trip).addOnSuccessListener {
            binding.tripNameEditText.text?.clear()
            binding.destinationEditText.text?.clear()
            binding.dateEditText.text?.clear()
            binding.descriptionEditText.text?.clear()
            binding.phoneEditText.text?.clear()
            binding.countryEditText.text?.clear()

            Toast.makeText(requireContext(), "Successfully Saved", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLocation() {
        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object: CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return CancellationTokenSource().token
            }

            override fun isCancellationRequested(): Boolean {
                return false
            }
        }).addOnSuccessListener { location: Location? ->
            if(location == null) {
                Toast.makeText(requireContext(), "Can not get location", Toast.LENGTH_SHORT).show()
            } else {
                setLocation(location)
            }
        }.addOnFailureListener {
            if(longitude == 0 && latitue == 0) {
                getLocation()
            }
        }
    }

    private fun setLocation(it: Location) {
        longitude = it.longitude
        latitue= it.latitude
        Log.d("Test", it.toString())
    }

    private fun inputFocusListener(binding: FragmentAddBinding) {

        //Trip Name
        binding.tripNameEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.tripNameContainer.helperText = validTripName(binding)
            }
        }

        //Destination
        binding.destinationEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.destinationContainer.helperText = validDestination(binding)
            }
        }

        //Date
        binding.dateEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.dateContainer.helperText = validDate(binding)
            }
        }

        //Country
        binding.countryEditText.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                binding.countryContainer.helperText = validCountry(binding)
            }
        }
    }

    private fun queryTrips() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val itemList = mutableListOf<String>()
                    val trip : kotlin.collections.List<ShowTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ShowTrip::class.java)!!
                    }

                    for (item in trip) {
                         itemList.add(item.tripName!!)
                    }

                    tripNameList = itemList.toList()

                } catch (e: Exception) {

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun resetTripData() {
        val message = "Confirm to reset"
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Confirm"){ _,_ ->
                database.removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reset Successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Reset Failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validTripName(binding: FragmentAddBinding): String? {
        val tripName = binding.tripNameEditText.text.toString()
        if(tripName.isEmpty()) {
            return "Required*"
        }

        if(tripName.trim() in tripNameList) {
            return "Trip Name already exist"
        }

        return null
    }


    private fun validDestination(binding: FragmentAddBinding): String? {
        val destination = binding.destinationEditText.text.toString()
        if(destination.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validDate(binding: FragmentAddBinding): String? {
        val date = binding.dateEditText.text.toString()
        if(date.isEmpty()) {
            return "Required*"
        }
        return null
    }

    private fun validCountry(binding: FragmentAddBinding): String? {
        val country = binding.countryEditText.text.toString()
        if(country.isEmpty()) {
            return "Required*"
        }
        return null
    }

}