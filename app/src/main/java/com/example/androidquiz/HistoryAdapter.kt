package com.example.androidquiz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.androidquiz.databinding.ItemSearchBinding

class HistoryAdapter (context: Context, list: List<MainActivity.Search>):
    ArrayAdapter<MainActivity.Search>(context, 0, list) {
    private lateinit var holder: ViewHolder
    private lateinit var listener: AdapterListener

    class ViewHolder(v: View) {
        val tvName = v.findViewById<TextView>(R.id.tvName)
        val tvAddress = v.findViewById<TextView>(R.id.tvAddress)
        val clPage = v.findViewById<ConstraintLayout>(R.id.clPage)
    }

    interface AdapterListener {
        fun onClick(item: MainActivity.Search)
    }

    fun setAdapterListener(l: AdapterListener) { listener = l }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView == null) {
            view = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val item = getItem(position) ?: return view

        holder.tvName.text = item.name
        holder.tvAddress.text = item.address

        holder.clPage.setOnClickListener { listener.onClick(item) }

        return view
    }
}