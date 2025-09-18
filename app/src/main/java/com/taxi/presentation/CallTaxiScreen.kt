package com.taxi.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.taxi.R
import kotlinx.coroutines.delay

enum class CallTaxiStatus { Calling, Assigned, Completed }

data class CallTaxiUiState(
    val status: CallTaxiStatus = CallTaxiStatus.Calling,
    val originText: String? = null,
    val destinationText: String? = null,
    val vehiclePlate: String? = null,
    val driverPhoto: android.graphics.Bitmap? = null,
    val carDescription: String? = null,
    val etaText: String? = null,
)

@Composable
fun CallTaxiScreen(
    state: CallTaxiUiState,
    onCancelCall: () -> Unit,
    onRegisterFavorite: () -> Unit,
    onConfirmDone: () -> Unit,
    onMarkDropOff: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state.status) {
            CallTaxiStatus.Calling -> {
                Text(
                    text = "택시를 부르고 있어요...",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
                Spacer(Modifier.height(32.dp))
                Button(onClick = onCancelCall, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) {
                    Text("호출 취소", style = MaterialTheme.typography.titleLarge)
                }
            }
            CallTaxiStatus.Assigned -> {
                LaunchedEffect(Unit) {
                    delay(3000)
                    onMarkDropOff()
                }
                // 차량 번호 가장 크게
                Text(
                    text = state.vehiclePlate ?: "--",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                // 기사님 사진 대신 벡터 리소스 아이콘 표시
                Image(
                    painter = painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = "기사님",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(96.dp)
                )
                Spacer(Modifier.height(12.dp))
                // 차종
                Text(
                    text = state.carDescription ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                // ETA
                if (!state.etaText.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.etaText,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(56.dp))
            }
            CallTaxiStatus.Completed -> {
                Text(
                    text = "편안하게 도착하셨나요?",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(56.dp))
                Button(onClick = onRegisterFavorite, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) {
                    Text(
                        "★ 이곳을 '자주 가는 곳'으로 등록하기",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onConfirmDone, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) {
                    Text("확인", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}


