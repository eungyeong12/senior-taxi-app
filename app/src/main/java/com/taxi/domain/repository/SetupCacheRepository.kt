package com.taxi.domain.repository

data class SetupCache(val lat: Double, val lng: Double, val snapshotPath: String)

interface SetupCacheRepository {
    suspend fun getCache(): SetupCache?
    suspend fun saveCache(cache: SetupCache)
    suspend fun clear()
}


