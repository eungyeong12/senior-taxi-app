package com.taxi.domain.repository

import com.taxi.domain.model.Taxi

interface TaxiRepository {
    suspend fun getNearbyTaxis(lat: Double, lng: Double): List<Taxi>
}


