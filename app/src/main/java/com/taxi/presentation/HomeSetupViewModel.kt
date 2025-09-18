package com.taxi.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxi.domain.repository.LatLng
import com.taxi.domain.repository.SetupCache
import com.taxi.domain.usecase.FetchPlacePhotoByQueryUseCase
import com.taxi.domain.usecase.GetCurrentLocationUseCase
import com.taxi.domain.usecase.GetHomeLocationUseCase
import com.taxi.domain.usecase.GetOrCreateMapSnapshotUseCase
import com.taxi.domain.usecase.GetSetupCacheUseCase
import com.taxi.domain.usecase.ReverseGeocodeUseCase
import com.taxi.domain.usecase.SaveHomeLocationUseCase
import com.taxi.domain.usecase.SaveSetupCacheUseCase
import kotlinx.coroutines.launch

data class HomeSetupUiState(
    val home: LatLng? = null,
    val current: LatLng? = null,
    val address: String? = null,
    val addressPhoto: android.graphics.Bitmap? = null,
    val mapSnapshot: android.graphics.Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class HomeSetupViewModel(
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val getHomeLocation: GetHomeLocationUseCase,
    private val saveHomeLocation: SaveHomeLocationUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val fetchPlacePhotoByQuery: FetchPlacePhotoByQueryUseCase,
    private val getOrCreateMapSnapshot: GetOrCreateMapSnapshotUseCase? = null,
    private val getSetupCache: GetSetupCacheUseCase? = null,
    private val saveSetupCache: SaveSetupCacheUseCase? = null,
) : ViewModel() {

    var uiState: HomeSetupUiState = HomeSetupUiState()
        private set

    fun loadHome(onStateChanged: (HomeSetupUiState) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            onStateChanged(uiState)
            runCatching { getHomeLocation() }
                .onSuccess { saved ->
                    Log.d("HomeSetupVM", "loadHome success saved=${'$'}saved")
                    uiState = uiState.copy(isLoading = false, home = saved)
                    onStateChanged(uiState)
                }
                .onFailure { e ->
                    Log.e("HomeSetupVM", "loadHome failure: ${'$'}{e.message}", e)
                    uiState = uiState.copy(isLoading = false, errorMessage = e.message)
                    onStateChanged(uiState)
                }
        }
    }

    fun fetchSnapshotForCurrent(onStateChanged: (HomeSetupUiState) -> Unit) {
        val curr = uiState.current ?: return
        viewModelScope.launch {
            runCatching { getOrCreateMapSnapshot?.invoke(curr.latitude, curr.longitude) }
                .onSuccess { bmp ->
                    runCatching { saveSetupCache?.invoke(SetupCache(curr.latitude, curr.longitude, "cached")) }
                    uiState = uiState.copy(mapSnapshot = bmp)
                    onStateChanged(uiState)
                }
        }
    }

    fun fetchCurrent(onStateChanged: (HomeSetupUiState) -> Unit) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            onStateChanged(uiState)
            // try cache first
            runCatching { getSetupCache?.invoke() }
                .onSuccess { cache ->
                    if (cache != null) {
                        val bmp = runCatching { getOrCreateMapSnapshot?.invoke(cache.lat, cache.lng) }.getOrNull()
                        val current = LatLng(cache.lat, cache.lng)
                        uiState = uiState.copy(isLoading = false, current = current, mapSnapshot = bmp)
                        onStateChanged(uiState)
                        return@launch
                    }
                }

            runCatching { getCurrentLocation() }
                .onSuccess { loc ->
                    val addr = runCatching { reverseGeocode(loc.latitude, loc.longitude) }.getOrNull()
                    uiState = uiState.copy(isLoading = false, current = loc, address = addr)
                    onStateChanged(uiState)
                }
                .onFailure { e ->
                    Log.e("HomeSetupVM", "fetchCurrent failure: ${'$'}{e.message}", e)
                    uiState = uiState.copy(isLoading = false, errorMessage = e.message)
                    onStateChanged(uiState)
                }
        }
    }

    fun saveCurrentAsHome(onStateChanged: (HomeSetupUiState) -> Unit, onSaved: () -> Unit) {
        val current = uiState.current ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            onStateChanged(uiState)
            runCatching { saveHomeLocation(current) }
                .onSuccess {
                    uiState = uiState.copy(isLoading = false, home = current)
                    onStateChanged(uiState)
                    onSaved()
                }
                .onFailure { e ->
                    uiState = uiState.copy(isLoading = false, errorMessage = e.message)
                    onStateChanged(uiState)
                }
        }
    }
}


