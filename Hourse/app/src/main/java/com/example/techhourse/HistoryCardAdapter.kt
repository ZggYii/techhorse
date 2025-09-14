package com.example.techhourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryCardAdapter(
    private val phoneCards: List<PhoneCard>,
    private val onItemClick: (PhoneCard) -> Unit
) : RecyclerView.Adapter<HistoryCardAdapter.HistoryCardViewHolder>() {

    class HistoryCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoneImage: ImageView = itemView.findViewById(R.id.iv_phone_image)
        val tvPhoneName: TextView = itemView.findViewById(R.id.tv_phone_name)
        val tvPhonePrice: TextView = itemView.findViewById(R.id.tv_phone_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_card, parent, false)
        return HistoryCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryCardViewHolder, position: Int) {
        val phoneCard = phoneCards[position]
        
        holder.ivPhoneImage.setImageResource(phoneCard.imageResource)
        holder.tvPhoneName.text = phoneCard.name
        holder.tvPhonePrice.text = phoneCard.price
        
        holder.itemView.setOnClickListener {
            onItemClick(phoneCard)
        }
    }

    override fun getItemCount(): Int = phoneCards.size
}
