package com.example.cw1native.models

import java.sql.Time
import java.util.*

data class Expense(
    var amount : Number ?= null,
    var expenseType : String ?= null,
    var date : Date ?= null,
    var time : Time ?=  null,
    var comment : String ?= null
)
