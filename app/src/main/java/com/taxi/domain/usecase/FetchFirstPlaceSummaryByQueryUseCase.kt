package com.taxi.domain.usecase

import com.taxi.domain.repository.PlacesRepository

class FetchFirstPlaceSummaryByQueryUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(query: String): PlacesRepository.PlaceSummary? = repository.fetchFirstPlaceSummaryByQuery(query)
}


