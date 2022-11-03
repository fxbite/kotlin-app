package com.example.cw1native.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cw1native.repository.TripRepository

class TripViewModel: ViewModel() {

    private val repository : TripRepository
    private  val _allTrips = MutableLiveData<List<Trip>>()
    val allTrips : LiveData<List<Trip>> = _allTrips

    init {
        repository = TripRepository().getInstance()
        repository.loadTrips(_allTrips)
    }
}