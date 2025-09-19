package com.taxi.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.taxi.data.geocoding.AndroidGeocodingRepository
import com.taxi.data.location.FusedLocationRepository
import com.taxi.data.home.HomeAddressRepositoryImpl
import com.taxi.domain.repository.HomeAddress
import com.taxi.domain.usecase.GetCurrentLocationUseCase
import com.taxi.domain.usecase.ReverseGeocodeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeSetupScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scope: CoroutineScope = remember { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main) }
) {
    val context = LocalContext.current

    val permissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val hasLocationPermission = remember {
        {
            permissions.all { perm ->
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val latState = remember { mutableStateOf<Double?>(null) }
    val lngState = remember { mutableStateOf<Double?>(null) }
    val addressState = remember { mutableStateOf<String?>(null) }

    val cameraPositionState: CameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        if (!hasLocationPermission()) {
            permissionLauncher.launch(permissions)
        }
        if (hasLocationPermission()) {
            val loc = runCatching { GetCurrentLocationUseCase(FusedLocationRepository(context)).invoke() }.getOrNull()
            if (loc != null) {
                latState.value = loc.latitude
                lngState.value = loc.longitude
                val addr = ReverseGeocodeUseCase(AndroidGeocodingRepository(context)).invoke(loc.latitude, loc.longitude)
                addressState.value = addr
            }
        }
    }

    LaunchedEffect(latState.value, lngState.value) {
        val lat = latState.value
        val lng = lngState.value
        if (lat != null && lng != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    com.google.android.gms.maps.model.LatLng(lat, lng),
                    17f
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            text = "현재 위치를 '우리집'으로 설정하시겠어요?",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(56.dp))
        Text(
            text = addressState.value ?: "위치 가져오는 중...",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(fraction = 0.9f)
        )
        Spacer(Modifier.height(16.dp))
        if (latState.value != null && lngState.value != null) {
            GoogleMap(
                modifier = Modifier.fillMaxWidth(fraction = 0.9f).height(260.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission()),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                Marker(state = MarkerState(position = com.google.android.gms.maps.model.LatLng(latState.value!!, lngState.value!!)))
            }
        } else {
            Text(
                text = if (hasLocationPermission()) "현재 위치를 찾는 중입니다..." else "위치 권한이 필요합니다.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(fraction = 0.9f)
            )
        }
        Spacer(Modifier.height(56.dp))
        Button(
            onClick = {
                val lat = latState.value
                val lng = lngState.value
                val addr = addressState.value
                if (lat != null && lng != null && !addr.isNullOrBlank()) {
                    scope.launch {
                        HomeAddressRepositoryImpl(context).saveHome(HomeAddress(lat, lng, addr))
                        snackbarHostState.showSnackbar("저장되었습니다")
                        onSaved()
                    }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("현재 위치를 먼저 확인해주세요") }
                }
            },
            modifier = Modifier.fillMaxWidth(fraction = 0.9f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) { Text("저장", style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(fraction = 0.9f)
        ) { Text("뒤로가기", style = MaterialTheme.typography.titleLarge) }

        SnackbarHost(hostState = snackbarHostState)
    }
}


