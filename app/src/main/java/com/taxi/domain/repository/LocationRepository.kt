package com.taxi.domain.repository

data class LatLng(val latitude: Double, val longitude: Double)

interface LocationRepository {
    suspend fun getCurrentLocation(): LatLng
}


