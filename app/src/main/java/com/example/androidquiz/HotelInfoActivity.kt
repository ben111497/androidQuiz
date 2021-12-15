package com.example.androidquiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.androidquiz.databinding.ActivityHotelInfoBinding

class HotelInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotelInfoBinding
    private lateinit var data: Info
    private lateinit var adapter: PictureSmallAdapter
    private lateinit var adapter2: PictureBigAdapter

    class Info(val photo: String, val name: String, val vicinity: String, val star: Int, val landscape: ArrayList<String>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHotelInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.extras?.let {
            val photo = it.getString("Photo") ?: ""
            val name = it.getString("Name") ?: ""
            val vicinity = it.getString("Vicinity") ?: ""
            val star = it.getInt("Star")
            val landscape = it.getStringArrayList("Landscape") ?: return@let

            data = Info(photo, name, vicinity, star, landscape)
        }

        init()
        setGridView()
        setListener()
        setLandscape()
    }

    override fun onBackPressed() {
        if (binding.clLandscape.visibility == View.VISIBLE) binding.clLandscape.visibility = View.GONE else super.onBackPressed()
    }

    private fun init() {
        binding.tvName.text = data.name
        binding.tvAddress.text = data.vicinity
        binding.tvStar.text = "${data.star}"
        binding.tvLandscapeNumber.text = "景觀圖(${data.landscape.size})"


        Glide.with(this).load(data.photo).into(binding.imgPhoto)
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener { finish() }
        binding.imgBack2.setOnClickListener { binding.clLandscape.visibility = View.GONE }
    }

    private fun setGridView() {
        binding.gvPicture.numColumns = 3

        adapter = PictureSmallAdapter(this, data.landscape)
        adapter.setAdapterListener(object: PictureSmallAdapter.AdapterListener {
            override fun onClick(position: Int) {
                binding.vpPicture.setCurrentItem(position, false)
                binding.clLandscape.visibility = View.VISIBLE
            }
        })

        binding.gvPicture.adapter = adapter
    }

    private fun setLandscape() {
        adapter2 = PictureBigAdapter(this, data.landscape)
        binding.vpPicture.adapter = adapter2

        binding.vpPicture.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tvPosition.text = "${position + 1}/${data.landscape.size}"
            }
        })
    }
}