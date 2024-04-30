package com.example.ironvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val items: MutableList<Item>, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textURL: TextView = itemView.findViewById(R.id.textURL)
        private val textUsername: TextView = itemView.findViewById(R.id.textUsername)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val clickableLayout: RelativeLayout = itemView.findViewById(R.id.clickableLayout)
        fun bind(item: Item) {
            textURL.text = item.url
            textUsername.text = item.username
            editButton.setOnClickListener {
                val dialogFragment = EditElementFragment()
                val bundle = Bundle().apply {
                    putString("url", item.url)
                    putString("username", item.username)
                    putString("password", item.password)
                }
                dialogFragment.arguments = bundle
                dialogFragment.show(fragmentManager, "EditElementFragment")
            }


            clickableLayout.setOnClickListener {
                val dialogFragment = EditElementFragment()
                val bundle = Bundle().apply {
                    putString("url", item.url)
                    putString("username", item.username)
                    putString("password", item.password)
                }
                dialogFragment.arguments = bundle
                dialogFragment.show(fragmentManager, "EditElementFragment")
            }
        }
    }
}
