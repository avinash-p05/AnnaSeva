package com.techelites.annaseva

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.auth.Donation

class DonationAdapter(private val donationList: List<Donation>) :
    RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donationList[position]
        holder.bind(donation)
    }

    override fun getItemCount(): Int {
        return donationList.size
    }

    inner class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        private val expiryDateTextView: TextView = itemView.findViewById(R.id.expiryDate)
        private val donationStatusTextView: TextView = itemView.findViewById(R.id.donationStatus)
        private val datePostedTextView: TextView = itemView.findViewById(R.id.datePosted)

        fun bind(donation: Donation) {
            nameTextView.text = donation.name
            quantityTextView.text = donation.quantity.toString()
            expiryDateTextView.text = donation.expiry
            donationStatusTextView.text = donation.donationStatus
            datePostedTextView.text = donation.createdAt
        }
    }
}