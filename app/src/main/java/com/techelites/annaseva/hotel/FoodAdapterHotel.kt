package com.techelites.annaseva.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.techelites.annaseva.Donation
import com.techelites.annaseva.R

class FoodAdapterHotel(
    private val context: Context,
    private var foodList: List<Donation>,
    private val itemClickListener: (Donation) -> Unit
) : RecyclerView.Adapter<FoodAdapterHotel.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.food_hotel, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodCreated.text = foodItem.createdAt
        holder.bind(foodItem, itemClickListener)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    fun updateList(newList: List<Donation>) {
        foodList = newList
        notifyDataSetChanged()
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.nameH)
        val foodCreated: TextView = itemView.findViewById(R.id.dateH)

        fun bind(foodItem: Donation, clickListener: (Donation) -> Unit) {
            itemView.setOnClickListener { clickListener(foodItem) }
        }
    }
}
