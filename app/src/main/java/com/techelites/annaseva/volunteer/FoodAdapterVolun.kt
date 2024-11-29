package com.techelites.annaseva.volunteer

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

class FoodAdapterVolun(
    private var recipes: ArrayList<FoodNgo>,
    private val context: Context,
    private val listener: VolunteerFoodListings
) : RecyclerView.Adapter<FoodAdapterVolun.ViewHolder>() {

    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: FoodNgo)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val nameF: TextView = itemView.findViewById(R.id.nameF)
        val hotelF: TextView = itemView.findViewById(R.id.hotelF)
        val dateF: TextView = itemView.findViewById(R.id.dateF)
        val locationF: TextView = itemView.findViewById(R.id.locationF)
        val imageView: ImageView = itemView.findViewById(R.id.recipeImage2)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onRecipeClick(recipes[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.food_volun, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]

        // Bind data to views
        holder.nameF.text = recipe.name
        holder.hotelF.text = recipe.hotel.name
        holder.dateF.text = recipe.createdAt

        // Load image using Picasso
        val imageUrl = "http://annaseva.ajinkyatechnologies.in/${recipe.imageUrl}"
        Picasso.get().load(imageUrl).into(holder.imageView)

        // Geocode location and update locationF TextView
        recipe.hotel.location.coordinates?.let {
            geocodeLocation(it.toString(), object : GeocodeCallback {
                override fun onGeocodeSuccess(formattedLocation: String) {
                    Handler(Looper.getMainLooper()).post {
                        holder.locationF.text = formattedLocation
                    }
                }

                override fun onGeocodeFailure(message: String) {
                    Handler(Looper.getMainLooper()).post {
                        holder.locationF.text = "Location not available"
                    }
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    fun updateRecipes(newRecipes: List<FoodNgo>) {
        recipes.clear()
        recipes.addAll(newRecipes)
        notifyDataSetChanged()
    }

    private fun geocodeLocation(location: String, callback: GeocodeCallback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.opencagedata.com/geocode/v1/json?q=${location}&key=${openCageApiKey}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Geocode", "Failed to geocode location: ${e.message}")
                callback.onGeocodeFailure("Failed to geocode location")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.use { responseBody ->
                        val responseString = responseBody.string()
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        val resultsArray = jsonObject.getAsJsonArray("results")
                        if (resultsArray.size() > 0) {
                            val formattedLocation = resultsArray[0].asJsonObject.get("formatted").asString
                            callback.onGeocodeSuccess(formattedLocation)
                        } else {
                            callback.onGeocodeFailure("No results found")
                        }
                    }
                } else {
                    Log.e("Geocode", "Geocode request failed: ${response.message}")
                    callback.onGeocodeFailure("Geocode request failed")
                }
            }
        })
    }

    interface GeocodeCallback {
        fun onGeocodeSuccess(formattedLocation: String)
        fun onGeocodeFailure(message: String)
    }
}
