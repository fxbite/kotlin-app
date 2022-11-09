package com.example.cw1native.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cw1native.repository.TripRepository

class ShowTripViewModel : ViewModel() {

    private val repository : TripRepository
    private val _allTrips = MutableLiveData<List<ShowTrip>>()
    val allTrips : LiveData<List<ShowTrip>> = _allTrips


    init {

        repository = TripRepository().getInstance()
        repository.loadTrips(_allTrips)

    }

}