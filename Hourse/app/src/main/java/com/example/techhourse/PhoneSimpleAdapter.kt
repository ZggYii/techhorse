package com.example.techhourse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.techhourse.database.entity.PhoneEntity

class PhoneSimpleAdapter(
    private var phoneList: List<PhoneEntity>,
    private val onPhoneClick: (PhoneEntity) -> Unit
) : RecyclerView.Adapter<PhoneSimpleAdapter.PhoneViewHolder>() {

    private var filteredList: List<PhoneEntity> = phoneList

    class PhoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPhoneName: TextView = itemView.findViewById(R.id.tv_phone_name)
        val tvPhonePrice: TextView = itemView.findViewById(R.id.tv_phone_price)
        val tvPhoneMemory: TextView = itemView.findViewById(R.id.tv_phone_memory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_simple, parent, false)
        return PhoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        val phone = filteredList[position]
        
        holder.tvPhoneName.text = "${phone.phoneModel}"
        holder.tvPhonePrice.text = phone.price
        holder.tvPhoneMemory.text = phone.memoryConfig
        
        holder.itemView.setOnClickListener {
            onPhoneClick(phone)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun updatePhones(newPhones: List<PhoneEntity>) {
        phoneList = newPhones
        filteredList = newPhones
        notifyDataSetChanged()
    }

    fun filterPhones(allPhones: List<PhoneEntity>, query: String) {
        filteredList = if (query.isEmpty()) {
            allPhones
        } else {
            allPhones.filter { phone ->
                phone.brandName.contains(query, ignoreCase = true) ||
                phone.phoneModel.contains(query, ignoreCase = true) ||
                phone.price.contains(query, ignoreCase = true) ||
                phone.memoryConfig.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}