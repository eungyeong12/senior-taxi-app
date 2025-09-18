package com.taxi.data.home

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.taxi.domain.repository.HomeRepository
import com.taxi.domain.repository.LatLng
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "home_prefs")

class DatastoreHomeRepository(
    private val context: Context
) : HomeRepository {

    private val KEY_HOME_LAT = doublePreferencesKey("home_lat")
    private val KEY_HOME_LNG = doublePreferencesKey("home_lng")

    override suspend fun getHome(): LatLng? {
        val prefs = context.dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val lat = prefs[KEY_HOME_LAT]
        val lng = prefs[KEY_HOME_LNG]
        return if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    override suspend fun saveHome(location: LatLng) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_LAT] = location.latitude
            prefs[KEY_HOME_LNG] = location.longitude
        }
    }

    override suspend fun clearHome() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_HOME_LAT)
            prefs.remove(KEY_HOME_LNG)
        }
    }
}


