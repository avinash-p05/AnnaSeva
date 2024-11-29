package com.techelites.annaseva.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.R
import com.techelites.annaseva.RequestData
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RequestAdapter(
    private val context: Context,
    private var requestsList: MutableList<RequestData>,
    private val dialogFragment: DialogFragment // Add this parameter
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestsList[position]
        holder.ngoName.text = request.ngoName
        holder.acceptButton.setOnClickListener {
            showConfirmationDialog(request.id, "accept")
        }
        holder.rejectButton.setOnClickListener {
            showConfirmationDialog(request.id, "reject")
        }
    }

    override fun getItemCount(): Int = requestsList.size

    fun updateList(newRequests: MutableList<RequestData>) {
        requestsList = newRequests
        notifyDataSetChanged()
    }

    private fun showConfirmationDialog(requestId: String, action: String) {
        val dialogBuilder = android.app.AlertDialog.Builder(context)
        dialogBuilder.setMessage("Are you sure you want to $action this request?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                when (action) {
                    "accept" -> acceptRequest(requestId)
                    "reject" -> rejectRequest(requestId)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Confirm $action")
        alert.show()
    }

    private fun acceptRequest(requestId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/donation/acceptRequest/$requestId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, "Accepted")
            }

            override fun onFailure(call: Call, e: IOException) {
                showErrorToast("Failed to accept request")
            }
        })
    }

    private fun rejectRequest(requestId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/donation/rejectRequest/$requestId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, "Rejected")
            }

            override fun onFailure(call: Call, e: IOException) {
                showErrorToast("Failed to reject request")
            }
        })
    }

    private fun handleResponse(response: Response, action: String) {
        if (response.isSuccessful) {
            val responseData = response.body?.string()
            val jsonObject = JSONObject(responseData)
            if (jsonObject.getBoolean("success")) {
                (context as? FragmentActivity)?.runOnUiThread {
                    Toast.makeText(context, "$action successfully", Toast.LENGTH_SHORT).show()
                    dialogFragment.dismissAllowingStateLoss() // Directly dismiss the passed fragment
                }
            } else {
                showErrorToast(jsonObject.getString("message"))
            }
        } else {
            showErrorToast("Failed to $action request")
        }
    }

    private fun showErrorToast(message: String) {
        (context as? FragmentActivity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ngoName: TextView = itemView.findViewById(R.id.ngoName)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }
}
