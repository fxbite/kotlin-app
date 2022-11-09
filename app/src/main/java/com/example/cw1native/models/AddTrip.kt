package com.example.cw1native.models

data class AddTrip(
    val tripName : String,
    val destination : String,
    val date : String,
    val description : String? = null,
    val phone : String? = null,
    val country : String,
    val riskAssessment : String,
    val latitude: Number,
    val longitude: Number,
    val expense: Expense? = null
)
