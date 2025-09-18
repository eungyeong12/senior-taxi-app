package com.taxi.domain.repository

import com.google.android.libraries.places.api.model.AutocompletePrediction
import android.graphics.Bitmap

interface PlacesRepository {
    suspend fun autocomplete(query: String): List<AutocompletePrediction>
    suspend fun fetchFirstPhotoByQuery(query: String, maxWidth: Int = 800): Bitmap?
    suspend fun fetchPhotoByQuery(query: String, photoIndex: Int = 0, maxWidth: Int = 800): Bitmap?
    suspend fun fetchFirstAddressByQuery(query: String): String?
    data class PlaceSummary(val placeId: String, val name: String?, val address: String?)
    suspend fun fetchFirstPlaceSummaryByQuery(query: String): PlaceSummary?

    // Nearby-biased variants
    suspend fun fetchFirstPlaceSummaryByQueryNear(query: String, lat: Double, lng: Double, countries: List<String> = listOf("KR")): PlaceSummary?
    suspend fun fetchPhotoByQueryNear(query: String, lat: Double, lng: Double, photoIndex: Int = 0, maxWidth: Int = 800, countries: List<String> = listOf("KR")): Bitmap?

    // Choose nearest among top predictions by measuring distance to origin
    suspend fun fetchNearestPlaceSummaryByQuery(query: String, originLat: Double, originLng: Double, candidates: Int = 5, countries: List<String> = listOf("KR")): PlaceSummary?
    suspend fun fetchPhotoByPlaceId(placeId: String, photoIndex: Int = 0, maxWidth: Int = 800): Bitmap?
}


