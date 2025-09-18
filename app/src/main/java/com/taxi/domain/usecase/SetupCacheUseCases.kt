package com.taxi.domain.usecase

import com.taxi.domain.repository.SetupCache
import com.taxi.domain.repository.SetupCacheRepository

class GetSetupCacheUseCase(private val repo: SetupCacheRepository) {
    suspend operator fun invoke(): SetupCache? = repo.getCache()
}

class SaveSetupCacheUseCase(private val repo: SetupCacheRepository) {
    suspend operator fun invoke(cache: SetupCache) = repo.saveCache(cache)
}


