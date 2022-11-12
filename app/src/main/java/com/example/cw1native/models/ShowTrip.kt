package com.example.cw1native.models

import android.os.Parcel
import android.os.Parcelable

data class ShowTrip(
    val tripName : String? = null,
    val destination : String? = null,
    val date : String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tripName)
        parcel.writeString(destination)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShowTrip> {
        override fun createFromParcel(parcel: Parcel): ShowTrip {
            return ShowTrip(parcel)
        }

        override fun newArray(size: Int): Array<ShowTrip?> {
            return arrayOfNulls(size)
        }
    }
}
