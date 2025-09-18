package com.taxi.domain.repository

interface HomeRepository {
    suspend fun getHome(): LatLng?
    suspend fun saveHome(location: LatLng)
    suspend fun clearHome()
}


