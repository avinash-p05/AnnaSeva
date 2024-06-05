package com.techelites.annaseva.hotel

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.techelites.annaseva.R
import com.techelites.annaseva.Requestt
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RequestsDialogFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestsAdapter
    private lateinit var donationId: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_requests_dialog)
        recyclerView = dialog.findViewById(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RequestsAdapter(requireContext(), mutableListOf()) // Pass context and empty list
        recyclerView.adapter = adapter

        donationId = arguments?.getString("donationId") ?: ""
        loadRequests(donationId)

        return dialog
    }

    private fun loadRequests(donationId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/donation/getNgoNameMobile/$donationId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val requests = parseRequestsJson(body)

                requireActivity().runOnUiThread {
                    if (requests.isNotEmpty()) {
                        adapter.updateList(requests)
                    } else {
                        Log.e("RequestsDialogFragment1", "Invalid JSON response: ")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    // Handle request failure
                }
            }
        })
    }

    private fun parseRequestsJson(jsonString: String?): List<Requestt> {
        val requestsList = mutableListOf<Requestt>()
        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.has("requests")) {
                val jsonArray = jsonObject.getJSONArray("requests")
                for (i in 0 until jsonArray.length()) {
                    val requestObject = jsonArray.getJSONObject(i)
                    val request = Gson().fromJson(requestObject.toString(), Requestt::class.java)
                    requestsList.add(request)
                }
            } else {
                Log.e("RequestsDialogFragment", "Invalid JSON response: $jsonString")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return requestsList
    }

    companion object {
        fun newInstance(donationId: String): RequestsDialogFragment {
            val args = Bundle()
            args.putString("donationId", donationId)
            val fragment = RequestsDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
