package com.example.cw1native.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


class List : Fragment() {

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

        })
    }




}