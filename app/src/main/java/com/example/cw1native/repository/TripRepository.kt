package com.example.cw1native.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.cw1native.models.ShowTrip
import com.google.firebase.database.*
import java.lang.Exception

class TripRepository {

    val databaseReference : DatabaseReference = FirebaseDatabase.getInstance("https://cw1-native-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Trips")

    @Volatile private var INSTANCE : TripRepository ?= null

    fun getInstance() : TripRepository{
        return INSTANCE ?: synchronized(this){

            val instance = TripRepository()
            INSTANCE = instance
            instance
        }

    }


    fun loadTrips(tripList : MutableLiveData<List<ShowTrip>>){

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                try {

                    val _tripList : List<ShowTrip> = snapshot.children.map { dataSnapshot ->

                        dataSnapshot.getValue(ShowTrip::class.java)!!
                    }

                    tripList.postValue(_tripList)



                }catch (e : Exception){


                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })


    }

}