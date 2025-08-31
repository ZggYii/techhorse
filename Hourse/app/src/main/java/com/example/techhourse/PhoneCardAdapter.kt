package com.example.techhourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhoneCardAdapter(
    private val phoneCards: List<PhoneCard>,
    private val onItemClick: (PhoneCard) -> Unit
) : RecyclerView.Adapter<PhoneCardAdapter.PhoneCardViewHolder>() {

    class PhoneCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoneImage: ImageView = itemView.findViewById(R.id.iv_phone_image)
        val tvPhoneName: TextView = itemView.findViewById(R.id.tv_phone_name)
        val tvPhonePrice: TextView = itemView.findViewById(R.id.tv_phone_price)
        val tvPhoneDesc: TextView = itemView.findViewById(R.id.tv_phone_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_card, parent, false)
        return PhoneCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneCardViewHolder, position: Int) {
        val phoneCard = phoneCards[position]
        
        holder.ivPhoneImage.setImageResource(phoneCard.imageResource)
        holder.tvPhoneName.text = phoneCard.name
        holder.tvPhonePrice.text = phoneCard.price
        holder.tvPhoneDesc.text = phoneCard.description
        
        holder.itemView.setOnClickListener {
            onItemClick(phoneCard)
        }
    }

    override fun getItemCount(): Int = phoneCards.size
}
