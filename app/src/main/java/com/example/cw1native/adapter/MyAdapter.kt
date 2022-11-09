package com.example.cw1native.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cw1native.R
import com.example.cw1native.models.ShowTrip

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val tripList = ArrayList<ShowTrip>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_trip,
            parent,false
        )
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = tripList[position]

        holder.tripName.text = currentItem.tripName
        holder.destination.text = currentItem.destination
        holder.date.text = currentItem.date

    }

    override fun getItemCount(): Int {
        return tripList.size
    }

    fun updateTripList(tripList : List<ShowTrip>){
        this.tripList.clear()
        this.tripList.addAll(tripList)
        notifyDataSetChanged()

    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val tripName : TextView = itemView.findViewById(R.id.tripName)
        val destination : TextView = itemView.findViewById(R.id.destination)
        val date : TextView = itemView.findViewById(R.id.date)

    }

}