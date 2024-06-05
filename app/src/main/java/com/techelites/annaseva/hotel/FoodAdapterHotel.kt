package com.techelites.annaseva.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.auth.Donation
import com.techelites.annaseva.R

class FoodAdapterHotel(
    private val context: Context,
    private var foodList: List<Donation>,
    private val onViewDetailsClick: (Donation) -> Unit,
    private val onViewRequestsClick: (Donation) -> Unit
) : RecyclerView.Adapter<FoodAdapterHotel.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.food_hotel, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodCreated.text = foodItem.createdAt
        holder.viewDetailsButton.setOnClickListener { onViewDetailsClick(foodItem) }
        holder.viewRequestsButton.setOnClickListener { onViewRequestsClick(foodItem) }
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
        val viewDetailsButton: Button = itemView.findViewById(R.id.requests)
        val viewRequestsButton: Button = itemView.findViewById(R.id.details)
    }
}
