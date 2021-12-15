package com.example.androidquiz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.androidquiz.databinding.ItemPictureSmallBinding

class PictureSmallAdapter(context: Context, list: ArrayList<String>)
    : ArrayAdapter<String>(context, 0, list)  {
    private lateinit var holder: ViewHolder
    private lateinit var listener: AdapterListener

    class ViewHolder(v: View) {
        val imgPicture = v.findViewById<ImageView>(R.id.imgPicture)
    }

    interface AdapterListener {
        fun onClick(position: Int)
    }

    fun setAdapterListener(l: AdapterListener) { listener = l }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View

        if (convertView == null) {
            view = ItemPictureSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val item = getItem(position) ?: return view

        Glide.with(context).load(item).into(holder.imgPicture)
        holder.imgPicture.setOnClickListener { listener.onClick(position) }

        return view
    }
}