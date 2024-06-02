package com.techelites.annaseva.ngo

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
import com.techelites.annaseva.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class FoodAdapterNgo(
    private var recipes: ArrayList<FoodNgo>,
    private val context: Context,
    private val listener: OnRecipeClickListener
) : RecyclerView.Adapter<FoodAdapterNgo.ViewHolder>() {
    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: FoodNgo)
    }

    interface GeocodeCallback {
        fun onGeocodeSuccess(formattedLocation: String)
        fun onGeocodeFailure(message: String)
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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.food_ngoo, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.nameF.text = recipe.name
        holder.hotelF.text = recipe.hotelName
        holder.dateF.text = recipe.createdAt
        recipe.location?.let {
            geocodeLocation(it, object : GeocodeCallback {
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
}
