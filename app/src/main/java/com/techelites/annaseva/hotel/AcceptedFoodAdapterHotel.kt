package com.techelites.annaseva.hotel

import FoodHotel
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
import com.techelites.annaseva.ngo.FoodAdapterNgo.OnRecipeClickListener

class AcceptedFoodAdapterHotel(
    private var foodList: ArrayList<FoodHotel>,
    private val context: Context,
    private val listener: OnRecipeClickListener
) : RecyclerView.Adapter<AcceptedFoodAdapterHotel.FoodViewHolder>() {

    interface OnRecipeClickListener {
        fun onRecipeClick(donation: FoodHotel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.accepted_food_hotel, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.foodName.text = foodItem.name
        holder.foodCreated.text = foodItem.createdAt
        holder.recipient.text = "Avinash NGO"
        val imageUrl = foodItem.imageUrl
        Picasso.get().load(imageUrl).into(holder.foodImage)
    }

    override fun getItemCount(): Int = foodList.size

    fun updateList(newList: List<FoodHotel>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        val foodName: TextView = itemView.findViewById(R.id.nameH2)
        val foodImage : ImageView = itemView.findViewById(R.id.recipeImage2)
        val foodCreated: TextView = itemView.findViewById(R.id.dateH)
        val recipient: TextView = itemView.findViewById(R.id.requestedBy)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onRecipeClick(foodList[position])
            }
        }

    }
}
