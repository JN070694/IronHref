package com.ironhref.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UrlAdapter(
    private var entries: MutableList<UrlEntry>,
    private val onClickUrl: (UrlEntry) -> Unit,
    private val onLongPressUrl: (UrlEntry) -> Unit
) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

    inner class UrlViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.textTitle)
        val urlText: TextView = view.findViewById(R.id.textUrl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_url, parent, false)
        return UrlViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val entry = entries[position]
        holder.titleText.text = entry.title
        holder.urlText.text = entry.url
        holder.itemView.setOnClickListener { onClickUrl(entry) }
        holder.itemView.setOnLongClickListener {
            onLongPressUrl(entry)
            true
        }
    }

    override fun getItemCount() = entries.size

    fun updateData(newEntries: List<UrlEntry>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }
}