package com.taxi.data.geocoding

import android.content.Context
import android.location.Geocoder
import com.taxi.domain.repository.GeocodingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AndroidGeocodingRepository(
    private val context: Context
) : GeocodingRepository {
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocation(latitude, longitude, 1)
            results?.firstOrNull()?.getAddressLine(0)
        }.getOrNull()
    }
}


