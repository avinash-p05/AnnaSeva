package com.techelites.annaseva.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.R
import com.techelites.annaseva.Requestt
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class RequestsAdapter(
    private val context: Context,
    private var requestsList: List<Requestt>
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestsList[position]
        holder.ngoName.text = request.ngo.name
        holder.acceptButton.setOnClickListener { updateRequestStatus(request._id, "Accepted") }
        holder.rejectButton.setOnClickListener { updateRequestStatus(request._id, "Rejected") }
    }

    override fun getItemCount(): Int {
        return requestsList.size
    }

    fun updateList(newList: List<Requestt>) {
        requestsList = newList
        notifyDataSetChanged()
    }

    private fun updateRequestStatus(requestId: String, status: String) {
        val client = OkHttpClient()
        val jsonObject = JSONObject()
        jsonObject.put("requestId", requestId)
        jsonObject.put("status", status)
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/donation/request/status")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle success
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }
        })
    }

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ngoName: TextView = itemView.findViewById(R.id.ngoName)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }
}
