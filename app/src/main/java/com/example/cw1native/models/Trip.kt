package com.example.cw1native.models

import java.util.*

data class Trip(
    var tripName : String ?= null,
    var destination : String ?= null,
    var date : Date ?= null,
    var description : String,
    var phone : Number,
    var country : String ?= null
)
