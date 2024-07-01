package com.techelites.annaseva.hotel

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.techelites.annaseva.R


class HotelDashboard : Fragment() {
    private lateinit var TotalDonations : TextView
    private lateinit var TotalDonationsByCategory : TextView
    private lateinit var TotalDonationsByStatus : TextView
    private lateinit var MonthlyDonations : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_hotel_dashboard, container, false)



        return view
    }
}