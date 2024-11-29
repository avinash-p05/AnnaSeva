package com.techelites.annaseva.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.techelites.annaseva.R
import com.techelites.annaseva.auth.Donation

class FoodAdapterHotel(
    private val context: Context,
    private var foodList: List<Donation>,
    private val onViewDetailsClick: (Donation) -> Unit
) : RecyclerView.Adapter<FoodAdapterHotel.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.food_hotel, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodCreated.text = foodItem.createdAt
        val imageUrl = foodItem.imageUrl
        Picasso.get().load(imageUrl).into(holder.foodImage)

        holder.viewDetailsButton.setOnClickListener { onViewDetailsClick(foodItem) }
        holder.viewRequestsButton.setOnClickListener {
            val dialog = RequestsDialogFragment.newInstance(foodItem.id)
            dialog.show((context as FragmentActivity).supportFragmentManager, "RequestsDialog")
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<Donation>) {
        foodList = newList
        notifyDataSetChanged()
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.nameH)
        val foodImage : ImageView = itemView.findViewById(R.id.recipeImage2)
        val foodCreated: TextView = itemView.findViewById(R.id.dateH)
        val viewDetailsButton: Button = itemView.findViewById(R.id.details)
        val viewRequestsButton: Button = itemView.findViewById(R.id.requests)
    }
}
