package com.taxi.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HubCodeScreen(
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
) {
    val code = remember { mutableStateOf("") }

    fun press(d: String) {
        if (code.value.length < 4) code.value += d
    }
    fun backspace() {
        if (code.value.isNotEmpty()) code.value = code.value.dropLast(1)
    }
    fun clear() { code.value = "" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "거점 코드 입력",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        // Code boxes
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(4) { idx ->
                val filled = idx < code.value.length
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Text(
                        text = if (filled) code.value[idx].toString() else "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(x = (-2).dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Keypad rows
        @Composable
        fun RowDigits(vararg digits: String) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                digits.forEach { d ->
                    ElevatedButton(
                        onClick = { press(d) },
                        modifier = Modifier.size(80.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) { Text(d, style = MaterialTheme.typography.headlineSmall) }
                }
            }
        }

        RowDigits("1", "2", "3")
        Spacer(Modifier.height(12.dp))
        RowDigits("4", "5", "6")
        Spacer(Modifier.height(12.dp))
        RowDigits("7", "8", "9")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedButton(onClick = { clear() }, modifier = Modifier.size(80.dp)) { Text("지움") }
            ElevatedButton(
                onClick = { press("0") },
                modifier = Modifier.size(80.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) { Text("0", style = MaterialTheme.typography.headlineSmall) }
            ElevatedButton(onClick = { backspace() }, modifier = Modifier.size(80.dp)) {
                Icon(Icons.Filled.Backspace, contentDescription = "지우기")
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(code.value) },
            enabled = code.value.length == 4,
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) { Text("확인", style = MaterialTheme.typography.titleLarge) }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(fraction = 0.8f)) { Text("뒤로") }
    }
}


