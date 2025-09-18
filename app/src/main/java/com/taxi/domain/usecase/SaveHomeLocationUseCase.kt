package com.taxi.domain.usecase

import com.taxi.domain.repository.HomeRepository
import com.taxi.domain.repository.LatLng

class SaveHomeLocationUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(location: LatLng) = homeRepository.saveHome(location)
}


