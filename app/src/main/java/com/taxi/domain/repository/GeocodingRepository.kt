package com.taxi.domain.repository

interface GeocodingRepository {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
}


