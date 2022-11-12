package com.example.cw1native.fragment

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cw1native.adapter.MyAdapter
import com.example.cw1native.databinding.FragmentListBinding
import com.example.cw1native.models.ShowTrip
import com.example.cw1native.models.ShowTripViewModel
import com.google.firebase.database.*
import kotlin.collections.List
import com.example.cw1native.R
import com.example.cw1native.activity.DetailTrip
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class List<T> : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : ShowTripViewModel
    private lateinit var tripRecyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var database : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)

        //Connect database
        database = FirebaseDatabase.getInstance("https://cw1-native-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Trips")

        //Search view
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchFilter(newText)
                return false
            }
        })

        //Search Condition
        binding.searchCondition.setOnClickListener {
            val dialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.search_condition_dialog, null)
            val searchDialog =  AlertDialog.Builder(requireContext())
            searchDialog.setView(dialogLayout)
            searchDialog.show()

            val button = dialogLayout.findViewById<Button>(R.id.searchButton)
            val inputSearchDate = dialogLayout.findViewById<TextInputEditText>(R.id.searchDate)

            //Date Picker
            val myCalendar = Calendar.getInstance()
            val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLable(myCalendar, inputSearchDate)
            }

            inputSearchDate.setOnClickListener {
                DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            //Search
            button.setOnClickListener {

                val tripNameParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchTripName).text.toString()
                val destinationParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDestination).text.toString()
                val dateParam = dialogLayout.findViewById<TextInputEditText>(R.id.searchDate).text.toString()

                database.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val filteredItem = mutableListOf<ShowTrip>()
                            val trip : List<ShowTrip> = snapshot.children.map { dataSnapshot ->

                                dataSnapshot.getValue(ShowTrip::class.java)!!

                            }

                            searchCondition(tripNameParam, destinationParam, dateParam, filteredItem, trip)

                            val filteredTrips: List<ShowTrip> = filteredItem.toList()
                            adapter.updateTripList(filteredTrips)
                        } catch (e: Exception) {

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            }


        }

        return binding.root
    }

    private fun searchFilter(queryText: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val filteredItem = mutableListOf<ShowTrip>()
                    val trip : List<ShowTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ShowTrip::class.java)!!

                    }

                    for (item in trip) {
                        if (
                            item.tripName!!.lowercase().contains(queryText.lowercase()) ||
                            item.date!!.lowercase().contains(queryText.lowercase()) ||
                            item.destination!!.lowercase().contains(queryText.lowercase())) {
                            filteredItem.add(item)
                        }
                    }

                    val filteredTrips: List<ShowTrip> = filteredItem.toList()
                    adapter.updateTripList(filteredTrips)

                }catch (e : Exception){
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRecyclerView = binding.listView
        tripRecyclerView.layoutManager = LinearLayoutManager(context)
        tripRecyclerView.setHasFixedSize(true)
        adapter = MyAdapter()
        tripRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(ShowTripViewModel::class.java)

        viewModel.allTrips.observe(viewLifecycleOwner, Observer {

            adapter.updateTripList(it)

            adapter.onItemClick = {
                val intent = Intent(requireContext(), DetailTrip::class.java)
                intent.putExtra("trip", it)
                startActivity(intent)
            }
        })
    }

    private fun updateLable(myCalendar: Calendar, inputSearchDate: TextInputEditText) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        inputSearchDate.setText(sdf.format(myCalendar.time))
    }

    private fun searchCondition(tripNameParam: String, destinationParam: String,
        dateParam: String, filteredItem: MutableList<ShowTrip>, trip: List<ShowTrip>) {
        if(tripNameParam.isNotEmpty() && destinationParam.isEmpty() && dateParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isEmpty() && destinationParam.isNotEmpty() && dateParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isEmpty() && destinationParam.isEmpty() && dateParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.date!!.lowercase().contains(dateParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isNotEmpty() && destinationParam.isNotEmpty() && dateParam.isEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameParam.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isNotEmpty() && destinationParam.isEmpty() && dateParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isEmpty() && destinationParam.isNotEmpty() && dateParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.destination!!.lowercase().contains(destinationParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isNotEmpty() && destinationParam.isNotEmpty() && dateParam.isNotEmpty()) {
            for (item in trip) {
                if (
                    item.tripName!!.lowercase().contains(tripNameParam.lowercase()) &&
                    item.destination!!.lowercase().contains(destinationParam.lowercase()) &&
                    item.date!!.lowercase().contains(dateParam.lowercase())) {
                    filteredItem.add(item)
                }
            }
        }

        if(tripNameParam.isEmpty() && destinationParam.isEmpty() && dateParam.isEmpty()) {
            for (item in trip) {
                filteredItem.add(item)
            }
        }

    }


}