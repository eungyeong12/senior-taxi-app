package com.taxi.domain.repository

import android.graphics.Bitmap

interface MapSnapshotRepository {
    suspend fun getOrCreateSnapshot(lat: Double, lng: Double, zoom: Int = 17): Bitmap
}


