package com.taxi.domain.usecase

import android.graphics.Bitmap
import com.taxi.domain.repository.MapSnapshotRepository

class GetOrCreateMapSnapshotUseCase(
    private val repository: MapSnapshotRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double, zoom: Int = 17): Bitmap = repository.getOrCreateSnapshot(lat, lng, zoom)
}


