package com.taxi.domain.model

data class Taxi(
    val id: String,
    val driverName: String,
    val latitude: Double,
    val longitude: Double,
    val available: Boolean,
)


