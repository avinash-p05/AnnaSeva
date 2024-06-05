package com.example.annasevaapp

import androidx.fragment.app.Fragment

class FoodListings : Fragment() {

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: DonationAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_food_listings, container, false)
//
//        // Initialize the RecyclerView
//        recyclerView = view.findViewById(R.id.)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        // Parse the JSON response
//        val donationList = parseDonationList(jsonResponse)
//
//        // Initialize the adapter with the parsed donation list
//        adapter = DonationAdapter(donationList)
//        recyclerView.adapter = adapter
//
//        return view
//    }
//
//    private fun parseDonationList(jsonResponse: String): List<Donation> {
//        val gson = Gson()
//        val jsonObject = gson.fromJson(jsonResponse, JsonObject::class.java)
//        val donationArray = jsonObject.getAsJsonArray("donations")
//
//        val donationListType: Type = object : TypeToken<List<Donation>>() {}.type
//        return gson.fromJson(donationArray, donationListType)
//    }
}