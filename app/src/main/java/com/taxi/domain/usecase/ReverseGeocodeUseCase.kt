package com.taxi.domain.usecase

import com.taxi.domain.repository.GeocodingRepository

class ReverseGeocodeUseCase(
    private val repository: GeocodingRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double): String? = repository.reverseGeocode(lat, lng)
}


