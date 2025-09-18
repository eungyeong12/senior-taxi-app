package com.taxi.data.repository

import com.taxi.domain.model.Taxi
import com.taxi.domain.repository.TaxiRepository
import kotlinx.coroutines.delay

class FakeTaxiRepository : TaxiRepository {
    override suspend fun getNearbyTaxis(lat: Double, lng: Double): List<Taxi> {
        delay(500)
        return listOf(
            Taxi(id = "1", driverName = "Kim", latitude = lat + 0.001, longitude = lng + 0.001, available = true),
            Taxi(id = "2", driverName = "Lee", latitude = lat - 0.001, longitude = lng - 0.001, available = true),
            Taxi(id = "3", driverName = "Park", latitude = lat + 0.002, longitude = lng - 0.001, available = false),
        )
    }
}


