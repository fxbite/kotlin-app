package com.example.cw1native.repository

import androidx.lifecycle.MutableLiveData
import com.example.cw1native.models.Trip
import com.google.firebase.database.*
import java.lang.Exception

class TripRepository {
    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("trips")

    @Volatile private var INSTANCE : TripRepository ?= null

    fun getInstance() : TripRepository {
        return INSTANCE ?: synchronized(this) {
            val instance = TripRepository()
            INSTANCE = instance
            instance
        }
    }

    fun loadTrips(tripList: MutableLiveData<List<Trip>>) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val _tripList : List<Trip> = snapshot.children.map {
                        dataSnapshot -> dataSnapshot.getValue(Trip::class.java)!!
                    }

                    tripList.postValue(_tripList)
                } catch (e : Exception) {

                }
            }

            override fun onCancelled(snapshot: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}