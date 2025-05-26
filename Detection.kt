package com.example.cnn_app

data class Detection(
    val id: Int,
    val filename: String,
    val class_id: Int,
    val confidence: Float,
    val bbox: List<Float>
)

