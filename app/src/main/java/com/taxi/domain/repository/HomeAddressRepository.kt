package com.taxi.domain.repository

data class HomeAddress(
    val latitude: Double,
    val longitude: Double,
    val addressLine: String,
)

interface HomeAddressRepository {
    suspend fun getHome(): HomeAddress?
    suspend fun saveHome(home: HomeAddress)
    suspend fun clear()
}


