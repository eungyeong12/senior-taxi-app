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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun DestinationChoiceScreen(
	onSpeak: () -> Unit,
    onPickFavorite: () -> Unit,
    onEnterCode: () -> Unit,
) {
	val context = LocalContext.current
	val permissions = remember {
		arrayOf(
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.RECORD_AUDIO
		)
	}

	val hasAllPermissions = remember {
		{
			permissions.all { perm ->
				ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
			}
		}
	}

	val launcher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestMultiplePermissions()
	) { _ ->
		// no-op; 버튼 동작에서 다시 분기 처리
	}

	LaunchedEffect(Unit) {
		if (!hasAllPermissions()) launcher.launch(permissions)
	}
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			text = "어디로 가시나요?",
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.fillMaxWidth(),
			textAlign = TextAlign.Center
		)
		Spacer(Modifier.height(60.dp))
		Button(
			onClick = { if (hasAllPermissions()) onSpeak() else launcher.launch(permissions) },
			modifier = Modifier.fillMaxWidth(fraction = 0.8f),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.secondary,
				contentColor = MaterialTheme.colorScheme.onSecondary
			)
		) {
			Icon(imageVector = Icons.Filled.Mic, contentDescription = null)
			Spacer(Modifier.height(0.dp))
			Text("  음성으로 말하기", style = MaterialTheme.typography.titleLarge)
		}
		Spacer(Modifier.height(32.dp))
		Button(
			onClick = onPickFavorite,
			modifier = Modifier.fillMaxWidth(fraction = 0.8f),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.secondary,
				contentColor = MaterialTheme.colorScheme.onSecondary
			)
		) {
			Icon(imageVector = Icons.Filled.Star, contentDescription = null)
			Spacer(Modifier.height(0.dp))
			Text("  자주 가는 곳에서 선택", style = MaterialTheme.typography.titleLarge)
		}
		Spacer(Modifier.height(32.dp))
		Button(
			onClick = onEnterCode,
			modifier = Modifier.fillMaxWidth(fraction = 0.8f),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.secondary,
				contentColor = MaterialTheme.colorScheme.onSecondary
			)
		) {
			Icon(imageVector = Icons.Filled.Pin, contentDescription = null)
			Spacer(Modifier.height(0.dp))
			Text("  거점 코드 입력하기", style = MaterialTheme.typography.titleLarge)
		}
	}
}


