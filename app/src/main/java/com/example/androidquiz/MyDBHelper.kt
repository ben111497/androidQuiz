package com.example.androidquiz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHelper(context: Context, name: String = database, factory: SQLiteDatabase.CursorFactory? = null, version: Int = v)
    : SQLiteOpenHelper(context, name, factory, version) {
    companion object {
        private const val database = "mdatabase.db" // 資料庫名稱
        private const val v = 2 //資料庫版本
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE myTable(HotelName text PRIMARY KEY,Address text NOT NULL, Lat REAL NOT NULL, Lng REAL NOT NULL)")
        //0.站名 1.經度 2.緯度 3.車站地址 4.車站id
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS myTable")
        onCreate(db)
    }
}