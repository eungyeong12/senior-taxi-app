package com.taxi.domain.usecase

import com.taxi.domain.repository.HomeRepository
import com.taxi.domain.repository.LatLng

class GetHomeLocationUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): LatLng? = homeRepository.getHome()
}


