package com.techelites.annaseva.hotel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.R
import com.techelites.annaseva.RequestData
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RequestsDialogFragment : DialogFragment() {

    private lateinit var donationId: String
    private lateinit var requestsAdapter: RequestAdapter
    private val requestsList = mutableListOf<RequestData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        donationId = arguments?.getString("donationId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requests_dialog, container, false)

        // Initialize RecyclerView and Adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        requestsAdapter = RequestAdapter(requireContext(), requestsList, this)
        recyclerView.adapter = requestsAdapter

        // Fetch requests after initializing the adapter
        fetchRequests(donationId)

        return view
    }


    private fun fetchRequests(donationId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/request/RequestsByDonationId/$donationId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val dataObject = jsonResponse.getJSONObject("data")

                    val requests = mutableListOf<RequestData>()
                    val totalRequests = dataObject.length()
                    var processedRequests = 0

                    dataObject.keys().forEach { key ->
                        val requestId = key
                        fetchRequestDetails(requestId) { requestData ->
                            requests.add(requestData)
                            processedRequests++
                            if (processedRequests == totalRequests) {
                                activity?.runOnUiThread {
                                    requestsList.clear()
                                    requestsList.addAll(requests)
                                    requestsAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                } else {
                    Log.e("RequestsDialogFragment", "Failed to retrieve requests")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("RequestsDialogFragment", "Failed to fetch requests", e)
            }
        })
    }

    private fun fetchRequestDetails(requestId: String, callback: (RequestData) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/request/$requestId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        val dataObject = jsonResponse.getJSONObject("data")

                        val requestData = RequestData(
                            id = dataObject.getString("id"),
                            donationId = dataObject.getString("donationId"),
                            ngoId = dataObject.getString("ngoId"),
                            ngoName = dataObject.getString("ngoName"),
                            status = dataObject.getString("status"),
                            priority = dataObject.getDouble("priority"),
                            createdAt = dataObject.getString("createdAt")
                        )

                        callback(requestData)
                    } else {
                        Log.e("RequestsDialogFragment", "Failed to retrieve request details")
                    }
                } else {
                    Log.e("RequestsDialogFragment", "Failed to retrieve request details, response code: ${response.code}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("RequestsDialogFragment", "Failed to fetch request details", e)
            }
        })
    }

    companion object {
        fun newInstance(donationId: String): RequestsDialogFragment {
            val fragment = RequestsDialogFragment()
            val args = Bundle()
            args.putString("donationId", donationId)
            fragment.arguments = args
            return fragment
        }
    }
    interface RefreshListener {
        fun onRefresh()
    }

}
