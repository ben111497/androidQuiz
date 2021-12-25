package com.example.androidquiz

class DataRes(val results: Result) {
    class Result(val content: ArrayList<Content>)
    class Content(val lat: Double, val lng: Double, val name: String,
                  val vicinity: String, val photo: String, val landscape: ArrayList<String>,
                  val star: Int)
}