package com.taxi.domain.usecase

import com.taxi.domain.repository.LatLng
import com.taxi.domain.repository.LocationRepository

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): LatLng = locationRepository.getCurrentLocation()
}


