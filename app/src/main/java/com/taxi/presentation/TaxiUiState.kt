package com.taxi.presentation

import com.taxi.domain.model.Taxi

data class TaxiUiState(
    val isLoading: Boolean = false,
    val taxis: List<Taxi> = emptyList(),
    val errorMessage: String? = null,
)