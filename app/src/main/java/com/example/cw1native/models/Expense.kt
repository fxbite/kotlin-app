package com.example.cw1native.models

import java.sql.Time
import java.util.*

data class Expense(
    var amount : String ?= null,
    var expenseType : String ?= null,
    var date : String ?= null,
    var time : String ?=  null,
    var comment : String ?= null
)
