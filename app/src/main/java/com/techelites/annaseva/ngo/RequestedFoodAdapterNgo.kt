package com.techelites.annaseva.ngo

import FoodNgo
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import com.techelites.annaseva.R
import okhttp3.*
import java.io.IOException

class RequestedFoodAdapterNgo(
    private var donations: ArrayList<FoodNgo>,
    private val context: Context,
    private val listener: OnRecipeClickListener
) : RecyclerView.Adapter<RequestedFoodAdapterNgo.ViewHolder>() {

    interface OnRecipeClickListener {
        fun onRecipeClick(donation: FoodNgo)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameF: TextView = itemView.findViewById(R.id.nameN2)
        val imageView: ImageView = itemView.findViewById(R.id.recipeImage2222)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onRecipeClick(donations[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.requested_food_ngo, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donation = donations[position]

        // Bind data to views
        holder.nameF.text = donation.name

        // Loadimage using Picasso
        val imageUrl = donation.imageUrl
        Picasso.get().load(imageUrl).into(holder.imageView)

    }

    override fun getItemCount(): Int {
        return donations.size
    }

    fun updateRecipes(newRecipes: List<FoodNgo>) {
        donations.clear()
        donations.addAll(newRecipes)
        notifyDataSetChanged()
    }

}
