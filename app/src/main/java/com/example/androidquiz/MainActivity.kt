package com.example.androidquiz

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.androidquiz.databinding.ActivityMainBinding
import com.example.androidquiz.databinding.DialogChooseBinding
import com.example.androidquiz.databinding.DialogSearchResultBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Marker

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val REQUEST_PERMISSIONS = 1
    private lateinit var binding: ActivityMainBinding
    private lateinit var data: DataRes
    private lateinit var googleMap: GoogleMap
    private var marks = ArrayList<MarkerOptions>()
    private var isInitialized = false
    private lateinit var dbrw: SQLiteDatabase

    private var locationLat: Double = 25.04
    private var locationLng: Double = 121.5

    private var localData = ArrayList<Search>()

    class Search(val name: String, val address: String, val lat: Double, val lng: Double)

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.extras?.getString("json")?.let {
                try {
                    data = Gson().fromJson(it, DataRes::class.java)
                    setData()
                } catch (e: Exception) {
                    Log.e("e", e.toString())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> for (result in grantResults) {
                //若使用者拒絕給予權限則關閉APP
                if (result != PackageManager.PERMISSION_GRANTED) {
                    finish()
                } else {
                    //連接MapFragment物件
                    val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                    map?.getMapAsync(this)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListener()
        setDB()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return

        getData()

        googleMap = map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationLat, locationLng), 12.0f))
        map.setOnMarkerClickListener(this)
        map.isMyLocationEnabled = true
    }

    private fun init() {
        //檢查使用者是否已授權定位權限，向使用者要求權限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS)
        else {
            //連接MapFragment物件
            val map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            map?.getMapAsync(this)
        }
    }

    private fun setListener() {
        binding.tvSearch.setOnClickListener { locationSearch() }
        binding.tvHistory.setOnClickListener { historyTable() }
        binding.imgClear.setOnClickListener { binding.edSearch.setText("") }
    }

    private fun setDB() {
        //初始化DB
        dbrw = MyDBHelper(this).writableDatabase

        val c = dbrw.rawQuery( "SELECT * FROM myTable",null)
        c.moveToFirst()
        for(i in 0 until c.count){
            localData.add(Search(c.getString(0), c.getString(1), c.getDouble(2), c.getDouble(3)))
            c.moveToNext()
        }

        c.close()
    }

    private fun locationSearch() {
        val text = binding.edSearch.text

        if (text.isBlank()) {
            Toast.makeText(this, "請輸入要搜尋的文字", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isInitialized) {
            Toast.makeText(this, "資料載入中\n請稍後... ", Toast.LENGTH_SHORT).show()
            return
        }

        val filterList = data.results.content.filter { it.name.contains(text) || it.vicinity.contains(text) }

        if (filterList.isEmpty()) {
            Toast.makeText(this, "查無相關資料", Toast.LENGTH_SHORT).show()
            return
        } else {
            Method.hideKeyBoard(this, binding.edSearch)
            showSearchDialog(filterList)
        }
    }

    private fun showSearchDialog(filterList: List<DataRes.Content>) {
        val view = DialogSearchResultBinding.inflate(LayoutInflater.from(this))
        DialogManager.instance.showCustom(this, view.root)?.let {
            val adapter = SearchAdapter(this, filterList)
            adapter.setAdapterListener(object: SearchAdapter.AdapterListener {
                override fun onClick(item: DataRes.Content) {
                    DialogManager.instance.cancelDialog()
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.lat, item.lng), 12.0f))

                    if (localData.none { it.name == item.name }) {
                        localData.add(Search(item.name, item.vicinity, item.lat, item.lng))
                        dbrw.execSQL("INSERT INTO myTable(HotelName, Address, Lat, Lng) VALUES(?,?,?,?)"
                            , arrayOf<Any?>(item.name, item.vicinity, item.lat, item.lng))
                    }
                }
            })

            view.lvSearch.adapter = adapter
        }
    }

    private fun historyTable() {
        if (localData.isEmpty()) {
            Toast.makeText(this, "無歷史紀錄", Toast.LENGTH_SHORT).show()
            return
        }

        showHistoryDialog(localData)
    }

    private fun showHistoryDialog(filterList: ArrayList<Search>) {
        val view = DialogSearchResultBinding.inflate(LayoutInflater.from(this))
        DialogManager.instance.showCustom(this, view.root)?.let {
            val adapter = HistoryAdapter(this, filterList)
            adapter.setAdapterListener(object: HistoryAdapter.AdapterListener {
                override fun onClick(item: Search) {
                    DialogManager.instance.cancelDialog()
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.lat, item.lng), 12.0f))
                }
            })

            view.lvSearch.adapter = adapter

            view.tvTitle.text = "搜尋紀錄"
            view.tcClear.visibility = View.VISIBLE
            view.tcClear.setOnClickListener {
                dbrw.execSQL("DELETE FROM myTable")
                localData.clear()
                Toast.makeText(this, "已清除紀錄", Toast.LENGTH_SHORT).show()
                DialogManager.instance.cancelDialog()
            }
        }
    }

    private fun setData() {
        try {
            // Request location update
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationLat = location.latitude
                    locationLng = location.longitude
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            (getSystemService(LOCATION_SERVICE) as? LocationManager)
                ?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, listener)

        } catch(ex: SecurityException) {
            Log.d("myTag", "Security Exception, no location available")
        }

        data.results.content.forEachIndexed { index, content ->
            val m1 = MarkerOptions()
            m1.position(LatLng(content.lat, content.lng))
            m1.title(content.name)
            m1.draggable(true)
            marks.add(m1)
            googleMap.addMarker(m1)
        }

        isInitialized = true
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val marker = p0 ?: return false

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 12.0f))
        showMarkClickDialog(marker)
        return false
    }

    private fun showMarkClickDialog(marker: Marker) {
        val view = DialogChooseBinding.inflate(LayoutInflater.from(this))
        DialogManager.instance.showCustom(this, view.root)?.let {
            view.tvTitle.text = marker.title

            view.llGoogle.setOnClickListener {
                val uri = Uri.parse("http://maps.google.com/maps?f=d&saddr=${locationLat}%20${locationLng}&daddr=${marker.position.latitude}%20${marker.position.longitude}&hl=en")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                DialogManager.instance.dismissAll()
            }

            view.llHotel.setOnClickListener {
                val info = data.results.content.find { it.name == marker.title } ?: return@setOnClickListener

                val b = Bundle()
                b.putString("Photo", info.photo)
                b.putString("Name", info.name)
                b.putString("Vicinity", info.vicinity)
                b.putInt("Star", info.star)
                b.putStringArrayList("Landscape", info.landscape)

                startActivity(Intent(this, HotelInfoActivity::class.java).putExtras(b))

                DialogManager.instance.cancelDialog()
            }
        }
    }

    private fun getData() {
        registerReceiver(receiver,IntentFilter("MyMessage"))

        val req = Request.Builder()
            .url("https://android-quiz-29a4c.web.app/")
            .build()

        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
               sendBroadcast(Intent("MyMessage").putExtra("json", response.body()?.string()))
            }
            override fun onFailure(call: Call, e: IOException?) {
                Log.e("查詢失敗","$e")
            }
        })
    }
}