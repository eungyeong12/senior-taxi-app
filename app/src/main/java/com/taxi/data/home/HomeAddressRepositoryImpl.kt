package com.taxi.data.home

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taxi.domain.repository.HomeAddress
import com.taxi.domain.repository.HomeAddressRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.homeDataStore by preferencesDataStore(name = "home_prefs")

class HomeAddressRepositoryImpl(
    private val context: Context
) : HomeAddressRepository {

    private object Keys {
        val LATITUDE: Preferences.Key<Double> = doublePreferencesKey("home_lat")
        val LONGITUDE: Preferences.Key<Double> = doublePreferencesKey("home_lng")
        val ADDRESS: Preferences.Key<String> = stringPreferencesKey("home_addr")
    }

    override suspend fun getHome(): HomeAddress? {
        val prefs = context.homeDataStore.data.map { it }.first()
        val lat = prefs[Keys.LATITUDE]
        val lng = prefs[Keys.LONGITUDE]
        val addr = prefs[Keys.ADDRESS]
        return if (lat != null && lng != null && addr != null) HomeAddress(lat, lng, addr) else null
    }

    override suspend fun saveHome(home: HomeAddress) {
        context.homeDataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = home.latitude
            prefs[Keys.LONGITUDE] = home.longitude
            prefs[Keys.ADDRESS] = home.addressLine
        }
    }

    override suspend fun clear() {
        context.homeDataStore.edit { it.clear() }
    }
}


