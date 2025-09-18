package com.taxi.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taxi.domain.usecase.FetchFirstPlaceSummaryByQueryUseCase
import com.taxi.domain.usecase.GetCurrentLocationUseCase
import com.taxi.domain.usecase.ReverseGeocodeUseCase
import com.taxi.presentation.CallTaxiScreen
import com.taxi.presentation.CallTaxiStatus
import com.taxi.presentation.CallTaxiUiState
import com.taxi.presentation.DestinationChoiceScreen
import com.taxi.presentation.FavoritePlacesScreen
import com.taxi.presentation.HomeSetupScreen
import com.taxi.presentation.HomeSetupUiState
import com.taxi.presentation.HomeSetupViewModel
import com.taxi.presentation.HubCodeScreen
import com.taxi.presentation.VoiceResultScreen
import kotlinx.coroutines.launch

object Routes {
    const val Choice = "choice"
    const val Favorites = "favorites"
    const val HomeSetup = "home_setup"
    const val VoiceResult = "voice_result"
    const val CallTaxi = "call_taxi"
    const val HubCode = "hub_code"
}

private fun sanitizeVoiceQuery(input: String): String {
    var q = input.trim()
    if (q.isEmpty()) return q
    // 불필요한 기호 제거
    q = q.replace(Regex("[\"'~!?.,]+"), " ")
    // 자주 쓰는 완곡어/불용어 제거
    q = q.replace(Regex("\\b(여기|저기|좀|한번|주세요)\\b"), " ")
    // 목적지 뒤의 동사/명령형 표현 제거 (…로 가자/갈래/가줘/가/가요/이동/검색/찾아/보여줘 등)
    q = q.replace(Regex("(까?지)?\\s*(으?로)?\\s*(가자|갈래|가 줘|가|가요|이동|이동해|검색|검색해|찾아|찾아줘|보여줘)\\s*$"), " ")
    // 여분 공백 정리
    q = q.replace(Regex("\\s+"), " ").trim()
    return if (q.isNotEmpty()) q else input.trim()
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    onRequestVoice: (onRecognized: (String?) -> Unit) -> Unit,
) {
    val favoritePlaces = listOf("우리집", "큰아들집", "병원", "은행", "영남대")
    val originText = remember { mutableStateOf<String?>(null) }
    val destinationText = remember { mutableStateOf<String?>(null) }
    val destinationName = remember { mutableStateOf<String?>(null) }
    val destinationPhoto = remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = Routes.Choice) {
        composable(Routes.Choice) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                DestinationChoiceScreen(
                    onSpeak = {
                        isLoading.value = true
                        onRequestVoice { text ->
                            if (text.isNullOrBlank()) { isLoading.value = false; return@onRequestVoice }
                            isLoading.value = true
                            scope.launch {
                                val original = text ?: ""
                                val sanitized = sanitizeVoiceQuery(original)
                                val mapped = when {
                                    original.contains("큰아들 집") -> "태왕한라아파트"
                                    original.contains("병원") -> "의료법인 근원의료재단 경상중앙병원"
                                    else -> sanitized
                                }
                                val repo = com.taxi.data.places.PlacesRepositoryImpl(context)
                                val voiceCandidates = when {
                                    mapped.contains("경산중방e편한세상1단지") -> listOf(
                                        "경산중방e편한세상1단지 아파트",
                                        "경산 중방 e편한세상 1단지",
                                        "e편한세상 경산중방 1단지"
                                    )
                                    mapped.contains("경상중앙병원") || mapped.contains("경상 중앙병원") || mapped.contains("의료법인 근원의료재단 경상중앙병원") -> listOf(
                                        "경상중앙병원",
                                        "의료법인 근원의료재단 경상중앙병원",
                                        "경산 중앙 병원"
                                    )
                                    else -> listOf(mapped)
                                }

                                var usedQuery = voiceCandidates.first()
                                var foundSummary: com.taxi.domain.repository.PlacesRepository.PlaceSummary? = null
                                var foundPhoto: android.graphics.Bitmap? = null
                                for (q in voiceCandidates) {
                                    val s = FetchFirstPlaceSummaryByQueryUseCase(repo)(q)
                                    val p1 = repo.fetchPhotoByQuery(q, photoIndex = 1, maxWidth = 800)
                                    val p = p1 ?: repo.fetchPhotoByQuery(q, photoIndex = 0, maxWidth = 800)
                                    if (s != null || p != null) {
                                        if (foundSummary == null && s != null) foundSummary = s
                                        if (foundPhoto == null && p != null) foundPhoto = p
                                        usedQuery = q
                                    }
                                    if (foundSummary != null && foundPhoto != null) break
                                }
                                destinationName.value = foundSummary?.name
                                destinationText.value = foundSummary?.address ?: usedQuery
                                destinationPhoto.value = foundPhoto

                                // 출발지 현재 위치 시도
                                runCatching {
                                    val locUc = GetCurrentLocationUseCase(com.taxi.data.location.FusedLocationRepository(context))
                                    val locNow = locUc()
                                    val revUc = ReverseGeocodeUseCase(com.taxi.data.geocoding.AndroidGeocodingRepository(context))
                                    val addr = revUc(locNow.latitude, locNow.longitude) ?: "${'$'}{locNow.latitude}, ${'$'}{locNow.longitude}"
                                    originText.value = "현재 위치($addr)"
                                }
                                isLoading.value = false
                                navController.navigate(Routes.VoiceResult)
                            }
                        }
                },
                    onPickFavorite = { navController.navigate(Routes.Favorites) },
                    onEnterCode = { navController.navigate(Routes.HubCode) }
                )

                if (isLoading.value) {
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        composable(Routes.Favorites) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                FavoritePlacesScreen(
                    favorites = favoritePlaces,
                    onSelect = { place ->
                        isLoading.value = true
                        scope.launch {
                            val mapped = when {
                                place.contains("큰아들집") -> "태왕한라아파트"
                                place.contains("병원") -> "의료법인 근원의료재단 경상중앙병원"
                                place.contains("은행") -> "대구은행ATM 구)정평동지점"
                                else -> place
                            }
                            val repo = com.taxi.data.places.PlacesRepositoryImpl(context)
                            val favCandidates = when {
                                mapped.contains("경산중방e편한세상1단지") -> listOf(
                                    "경산중방e편한세상1단지 아파트",
                                    "경산 중방 e편한세상 1단지",
                                    "e편한세상 경산중방 1단지"
                                )
                                mapped.contains("경상중앙병원") || mapped.contains("경상 중앙병원") || mapped.contains("의료법인 근원의료재단 경상중앙병원") -> listOf(
                                    "경상중앙병원",
                                    "의료법인 근원의료재단 경상중앙병원",
                                    "경산 중앙 병원"
                                )
                                else -> listOf(mapped)
                            }
                            var usedQuery = favCandidates.first()
                            var foundSummary: com.taxi.domain.repository.PlacesRepository.PlaceSummary? = null
                            var foundPhoto: android.graphics.Bitmap? = null
                            for (q in favCandidates) {
                                val s = FetchFirstPlaceSummaryByQueryUseCase(repo)(q)
                                val p1 = repo.fetchPhotoByQuery(q, photoIndex = 1, maxWidth = 800)
                                val p = p1 ?: repo.fetchPhotoByQuery(q, photoIndex = 0, maxWidth = 800)
                                if (s != null || p != null) {
                                    if (foundSummary == null && s != null) foundSummary = s
                                    if (foundPhoto == null && p != null) foundPhoto = p
                                    usedQuery = q
                                }
                                if (foundSummary != null && foundPhoto != null) break
                            }
                            destinationName.value = foundSummary?.name
                            destinationText.value = foundSummary?.address ?: usedQuery
                            destinationPhoto.value = foundPhoto

                            // 출발지 현재 위치 시도
                            runCatching {
                                val locUc = GetCurrentLocationUseCase(com.taxi.data.location.FusedLocationRepository(context))
                                val locNow = locUc()
                                val revUc = ReverseGeocodeUseCase(com.taxi.data.geocoding.AndroidGeocodingRepository(context))
                                val addr = revUc(locNow.latitude, locNow.longitude) ?: "${'$'}{locNow.latitude}, ${'$'}{locNow.longitude}"
                                originText.value = "현재 위치($addr)"
                            }
                            isLoading.value = false
                            navController.navigate(Routes.VoiceResult)
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onSetupHome = { navController.navigate(Routes.HomeSetup) }
                )

                if (isLoading.value) {
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        composable(Routes.HomeSetup) {
            val ctx = context
            val homeVm: HomeSetupViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val locationRepo = com.taxi.data.location.FusedLocationRepository(ctx)
                    val homeRepo = com.taxi.data.home.DatastoreHomeRepository(ctx)
                    val getCurrent = GetCurrentLocationUseCase(locationRepo)
                    val getHome = com.taxi.domain.usecase.GetHomeLocationUseCase(homeRepo)
                    val saveHome = com.taxi.domain.usecase.SaveHomeLocationUseCase(homeRepo)
                    val reverse = ReverseGeocodeUseCase(com.taxi.data.geocoding.AndroidGeocodingRepository(ctx))
                    val placesRepo = com.taxi.data.places.PlacesRepositoryImpl(ctx)
                    val fetchPhoto = com.taxi.domain.usecase.FetchPlacePhotoByQueryUseCase(placesRepo)
                    @Suppress("UNCHECKED_CAST")
                    return HomeSetupViewModel(getCurrent, getHome, saveHome, reverse, fetchPhoto) as T
                }
            })

            var setupState = remember { HomeSetupUiState() }
            HomeSetupScreen(
                addressText = setupState.address
                    ?: setupState.current?.let { "(${it.latitude}, ${it.longitude})" },
                currentLatLng = setupState.current,
                onUseCurrentLocation = { homeVm.fetchCurrent { setupState = it } },
                onSkip = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
        composable(Routes.HubCode) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                HubCodeScreen(
                    onConfirm = { code ->
                        isLoading.value = true
                        scope.launch {
                            // 1) 허브 코드 -> 검색어 매핑 (예: 0000 -> 팔공산)
                            val codeToQuery = mapOf(
                                "0000" to "팔공산"
                            )
                            val query = codeToQuery[code] ?: "거점 ${'$'}code"

                            // 2) Places에서 장소 요약/사진 조회
                            val placesRepo = com.taxi.data.places.PlacesRepositoryImpl(context)
                            val summary = FetchFirstPlaceSummaryByQueryUseCase(placesRepo)(query)
                            destinationName.value = summary?.name ?: query
                            destinationText.value = summary?.address ?: "입력하신 거점으로 안내합니다"
                            val photo1 = placesRepo.fetchPhotoByQuery(query, photoIndex = 1, maxWidth = 800)
                            destinationPhoto.value = photo1 ?: placesRepo.fetchPhotoByQuery(query, photoIndex = 0, maxWidth = 800)

                            // 3) 출발지: 현재 위치 텍스트 구성
                            runCatching {
                                val locUc = GetCurrentLocationUseCase(com.taxi.data.location.FusedLocationRepository(context))
                                val loc = locUc()
                                val revUc = ReverseGeocodeUseCase(com.taxi.data.geocoding.AndroidGeocodingRepository(context))
                                val addr = revUc(loc.latitude, loc.longitude) ?: "${'$'}{loc.latitude}, ${'$'}{loc.longitude}"
                                originText.value = "현재 위치($addr)"
                            }

                            isLoading.value = false
                            navController.navigate(Routes.VoiceResult)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )

                if (isLoading.value) {
                    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        composable(Routes.VoiceResult) {
            VoiceResultScreen(
                originText = originText.value,
                destinationText = listOfNotNull(destinationName.value, destinationText.value).joinToString("\n"),
                placePhoto = destinationPhoto.value,
                onConfirm = { navController.navigate(Routes.CallTaxi) },
                onRetry = { navController.popBackStack() }
            )
        }

        composable(Routes.CallTaxi) {
            // Demo state; 실제로는 ViewModel에서 상태 업데이트
            val uiState = remember {
                mutableStateOf(
                    CallTaxiUiState(
                        status = CallTaxiStatus.Calling,
                        originText = originText.value,
                        destinationText = destinationText.value
                    )
                )
            }
            // 5초 후 Assigned로 전환
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                uiState.value = uiState.value.copy(
                    status = CallTaxiStatus.Assigned,
                    vehiclePlate = "12가 3456",
                    carDescription = "은색 소나타",
                    etaText = "5분 뒤 도착"
                )
            }
            CallTaxiScreen(
                state = uiState.value,
                onCancelCall = { navController.popBackStack() },
                onRegisterFavorite = {
                    android.widget.Toast.makeText(context, "등록되었습니다", android.widget.Toast.LENGTH_SHORT).show()
                },
                onConfirmDone = { navController.popBackStack(Routes.Choice, inclusive = false) },
                onMarkDropOff = {
                    uiState.value = uiState.value.copy(status = CallTaxiStatus.Completed)
                }
            )
        }
    }
}


