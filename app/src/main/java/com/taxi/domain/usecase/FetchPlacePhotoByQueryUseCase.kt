package com.taxi.domain.usecase

import android.graphics.Bitmap
import com.taxi.domain.repository.PlacesRepository

class FetchPlacePhotoByQueryUseCase(
    private val repository: PlacesRepository
) {
    suspend operator fun invoke(query: String, maxWidth: Int = 800): Bitmap? = repository.fetchFirstPhotoByQuery(query, maxWidth)
}


