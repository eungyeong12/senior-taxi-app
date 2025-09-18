package com.taxi.data.places

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.taxi.BuildConfig
import com.taxi.domain.repository.PlacesRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlacesRepositoryImpl(context: Context) : PlacesRepository {
    private val client: PlacesClient
    private val tag = "PlacesRepo"

    init {
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, BuildConfig.GOOGLE_MAPS_KEY)
            Log.d(tag, "Places.initialize called. isInitialized=${Places.isInitialized()}")
        }
        client = Places.createClient(context)
        Log.d(tag, "PlacesClient created")
    }

    override suspend fun autocomplete(query: String): List<AutocompletePrediction> = suspendCancellableCoroutine { cont ->
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()
        Log.d(tag, "autocomplete start query='" + query + "'")
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                Log.d(tag, "autocomplete success count=" + response.autocompletePredictions.size)
                cont.resume(response.autocompletePredictions)
            }
            .addOnFailureListener { e ->
                Log.e(tag, "autocomplete failure: " + e.message, e)
                cont.resumeWithException(e)
            }
    }

    override suspend fun fetchFirstAddressByQuery(query: String): String? = suspendCancellableCoroutine { cont ->
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val first = response.autocompletePredictions.firstOrNull()
                if (first == null) {
                    cont.resume(null)
                    return@addOnSuccessListener
                }
                val placeId = first.placeId
                val placeFields = listOf(Place.Field.ADDRESS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        cont.resume(placeResponse.place.address)
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun fetchFirstPlaceSummaryByQuery(query: String): PlacesRepository.PlaceSummary? = suspendCancellableCoroutine { cont ->
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val first = response.autocompletePredictions.firstOrNull()
                if (first == null) {
                    cont.resume(null)
                    return@addOnSuccessListener
                }
                val placeId = first.placeId
                val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val p = placeResponse.place
                        cont.resume(PlacesRepository.PlaceSummary(placeId = first.placeId, name = p.name, address = p.address))
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun fetchFirstPhotoByQuery(query: String, maxWidth: Int): Bitmap? = suspendCancellableCoroutine { cont ->
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()
        Log.d(tag, "photoByQuery start query='" + query + "'")
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                Log.d(tag, "photoByQuery predictions=" + predictions.size)
                val first = predictions.firstOrNull()
                if (first == null) {
                    Log.d(tag, "photoByQuery no predictions")
                    cont.resume(null)
                    return@addOnSuccessListener
                }
                val placeId = first.placeId
                Log.d(tag, "using first placeId=" + placeId)
                val placeFields = listOf(Place.Field.PHOTO_METADATAS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val metas = placeResponse.place.photoMetadatas
                        Log.d(tag, "fetchPlace success photoMetas=" + (metas?.size ?: 0))
                        val metadata: PhotoMetadata? = metas?.firstOrNull()
                        if (metadata == null) {
                            Log.d(tag, "no photo metadata available for first prediction")
                            cont.resume(null)
                            return@addOnSuccessListener
                        }
                        val photoRequest = FetchPhotoRequest.builder(metadata)
                            .setMaxWidth(maxWidth)
                            .build()
                        client.fetchPhoto(photoRequest)
                            .addOnSuccessListener { photoResponse ->
                                val bmp = photoResponse.bitmap
                                Log.d(tag, "fetchPhoto success size=" + bmp.width + "x" + bmp.height)
                                cont.resume(bmp)
                            }
                            .addOnFailureListener { e ->
                                Log.e(tag, "fetchPhoto failure: " + e.message, e)
                                cont.resumeWithException(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(tag, "fetchPlace failure: " + e.message, e)
                        cont.resumeWithException(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "predictions failure: " + e.message, e)
                cont.resumeWithException(e)
            }
    }

    override suspend fun fetchPhotoByQuery(query: String, photoIndex: Int, maxWidth: Int): Bitmap? = suspendCancellableCoroutine { cont ->
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val first = response.autocompletePredictions.firstOrNull()
                if (first == null) {
                    cont.resume(null); return@addOnSuccessListener
                }
                val placeId = first.placeId
                val placeFields = listOf(Place.Field.PHOTO_METADATAS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val metas = placeResponse.place.photoMetadatas
                        val meta = metas?.getOrNull(photoIndex)
                        if (meta == null) { cont.resume(null); return@addOnSuccessListener }
                        val photoRequest = FetchPhotoRequest.builder(meta)
                            .setMaxWidth(maxWidth)
                            .build()
                        client.fetchPhoto(photoRequest)
                            .addOnSuccessListener { photoResponse -> cont.resume(photoResponse.bitmap) }
                            .addOnFailureListener { e -> cont.resumeWithException(e) }
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    // Removed old biasedPredictionsRequest helper (used deprecated API)

    override suspend fun fetchFirstPlaceSummaryByQueryNear(query: String, lat: Double, lng: Double, countries: List<String>): PlacesRepository.PlaceSummary? = suspendCancellableCoroutine { cont ->
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(
                com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                    com.google.android.gms.maps.model.LatLng(lat - 0.25, lng - 0.25),
                    com.google.android.gms.maps.model.LatLng(lat + 0.25, lng + 0.25)
                )
            )
            .setCountries(countries)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val first = response.autocompletePredictions.firstOrNull()
                if (first == null) { cont.resume(null); return@addOnSuccessListener }
                val placeId = first.placeId
                val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val p = placeResponse.place
                        cont.resume(PlacesRepository.PlaceSummary(placeId = first.placeId, name = p.name, address = p.address))
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun fetchNearestPlaceSummaryByQuery(query: String, originLat: Double, originLng: Double, candidates: Int, countries: List<String>): PlacesRepository.PlaceSummary? = suspendCancellableCoroutine { cont ->
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries(countries)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val preds = response.autocompletePredictions.take(candidates)
                if (preds.isEmpty()) { cont.resume(null); return@addOnSuccessListener }
                // Compute distance using Android Location API
                var best: Pair<AutocompletePrediction, Float>? = null
                preds.forEach { ap ->
                    // Need place latLng; fetch minimal fields to compute distance
                    val req = FetchPlaceRequest.newInstance(ap.placeId, listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS))
                    client.fetchPlace(req)
                        .addOnSuccessListener { pr ->
                            val ll = pr.place.latLng
                            if (ll != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(originLat, originLng, ll.latitude, ll.longitude, results)
                                val d = results[0]
                                val curr = best
                                if (curr == null || d < curr.second) {
                                    best = ap to d
                                }
                            }
                            // When last fetch returns, resolve best
                            if (ap == preds.last()) {
                                val sel = best?.first
                                if (sel == null) { cont.resume(null) } else {
                                    val req2 = FetchPlaceRequest.newInstance(sel.placeId, listOf(Place.Field.NAME, Place.Field.ADDRESS))
                                    client.fetchPlace(req2)
                                        .addOnSuccessListener { p2 ->
                                            val p = p2.place
                                            cont.resume(PlacesRepository.PlaceSummary(placeId = sel.placeId, name = p.name, address = p.address))
                                        }
                                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                                }
                            }
                        }
                        .addOnFailureListener { e -> cont.resumeWithException(e) }
                }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun fetchPhotoByPlaceId(placeId: String, photoIndex: Int, maxWidth: Int): Bitmap? = suspendCancellableCoroutine { cont ->
        val placeFields = listOf(Place.Field.PHOTO_METADATAS)
        val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
        client.fetchPlace(placeRequest)
            .addOnSuccessListener { placeResponse ->
                val metas = placeResponse.place.photoMetadatas
                val meta = metas?.getOrNull(photoIndex)
                if (meta == null) { cont.resume(null); return@addOnSuccessListener }
                val photoRequest = FetchPhotoRequest.builder(meta)
                    .setMaxWidth(maxWidth)
                    .build()
                client.fetchPhoto(photoRequest)
                    .addOnSuccessListener { photoResponse -> cont.resume(photoResponse.bitmap) }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    override suspend fun fetchPhotoByQueryNear(query: String, lat: Double, lng: Double, photoIndex: Int, maxWidth: Int, countries: List<String>): Bitmap? = suspendCancellableCoroutine { cont ->
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(
                com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                    com.google.android.gms.maps.model.LatLng(lat - 0.25, lng - 0.25),
                    com.google.android.gms.maps.model.LatLng(lat + 0.25, lng + 0.25)
                )
            )
            .setCountries(countries)
            .build()
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val first = response.autocompletePredictions.firstOrNull()
                if (first == null) { cont.resume(null); return@addOnSuccessListener }
                val placeId = first.placeId
                val placeFields = listOf(Place.Field.PHOTO_METADATAS)
                val placeRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                client.fetchPlace(placeRequest)
                    .addOnSuccessListener { placeResponse ->
                        val metas = placeResponse.place.photoMetadatas
                        val meta = metas?.getOrNull(photoIndex)
                        if (meta == null) { cont.resume(null); return@addOnSuccessListener }
                        val photoRequest = FetchPhotoRequest.builder(meta)
                            .setMaxWidth(maxWidth)
                            .build()
                        client.fetchPhoto(photoRequest)
                            .addOnSuccessListener { photoResponse -> cont.resume(photoResponse.bitmap) }
                            .addOnFailureListener { e -> cont.resumeWithException(e) }
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}


