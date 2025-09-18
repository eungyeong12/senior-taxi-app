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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomeSetupScreen(
    addressText: String?,
    currentLatLng: com.taxi.domain.repository.LatLng? = null,
    onUseCurrentLocation: () -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
) {
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val hasPermission = {
        permissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        if (granted) onUseCurrentLocation()
    }

    LaunchedEffect(Unit) {
        if (hasPermission()) {
            onUseCurrentLocation()
        } else {
            launcher.launch(permissions)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
			text = "현재 위치를 '우리집'으로 설정하시겠어요?",
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.fillMaxWidth(),
			textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(60.dp))
        Text(
			text = addressText ?: "현재 위치를 불러와 집 주소로 저장할 수 있어요",
			style = MaterialTheme.typography.headlineSmall,
			modifier = Modifier.fillMaxWidth(),
			textAlign = TextAlign.Center
        )
        val latLng = currentLatLng?.let { LatLng(it.latitude, it.longitude) }
        if (latLng != null) {
            Spacer(Modifier.height(16.dp))
            val camState = rememberCameraPositionState()
            if (camState.position.target != latLng) {
                camState.position = CameraPosition.fromLatLngZoom(latLng, 17f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxWidth(fraction = 0.9f).height(220.dp),
                cameraPositionState = camState
            ) {
                Marker(state = MarkerState(position = latLng), title = "현재 위치")
            }
        }
        Spacer(Modifier.height(60.dp))
        Button(onClick = onSave, enabled = addressText != null, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) { Text("저장", style = MaterialTheme.typography.titleLarge) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSkip, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) { Text("뒤로가기", style = MaterialTheme.typography.titleLarge) }
    }
}


