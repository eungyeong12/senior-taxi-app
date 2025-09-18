package com.taxi.domain.usecase

import com.taxi.domain.model.Taxi
import com.taxi.domain.repository.TaxiRepository

class GetNearbyTaxisUseCase(
    private val taxiRepository: TaxiRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double): List<Taxi> {
        return taxiRepository.getNearbyTaxis(lat, lng)
    }
}


