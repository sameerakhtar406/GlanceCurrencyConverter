package com.currencyconverter.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.currencyconverter.data.api.ExchangeRateApi
import com.currencyconverter.data.model.CachedRates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Currency
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_converter_prefs")

class CurrencyRepository(
    private val context: Context,
    private val api: ExchangeRateApi = ExchangeRateApi()
) {
    companion object {
        private val KEY_RATES_JSON = stringPreferencesKey("rates_json")
        private val KEY_LAST_SYNCED = longPreferencesKey("last_synced")
        private val KEY_HOME_CURRENCY = stringPreferencesKey("home_currency")
        private val KEY_TRAVEL_CURRENCY = stringPreferencesKey("travel_currency")
    }

    val homeCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_HOME_CURRENCY] ?: "INR"
    }

    val travelCurrency: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TRAVEL_CURRENCY] ?: "AED"
    }

    val cachedRates: Flow<CachedRates?> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_RATES_JSON] ?: return@map getFallbackRates()
        try {
            Json.decodeFromString<CachedRates>(jsonStr)
        } catch (e: Exception) {
            getFallbackRates()
        }
    }

    suspend fun setHomeCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOME_CURRENCY] = currency
        }
    }

    suspend fun setTravelCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TRAVEL_CURRENCY] = currency
        }
    }

    suspend fun syncRates(): Result<CachedRates> = runCatching {
        val response = api.getLatestRates("USD")
        val cached = CachedRates(
            lastUpdated = System.currentTimeMillis(),
            baseCurrency = response.base_code,
            rates = response.rates
        )
        context.dataStore.edit { prefs ->
            prefs[KEY_RATES_JSON] = Json.encodeToString(cached)
            prefs[KEY_LAST_SYNCED] = cached.lastUpdated
        }
        cached
    }

    suspend fun convert(amount: Double, sourceCurrency: String, targetCurrency: String): Double {
        if (sourceCurrency == targetCurrency) return amount
        
        val cached = cachedRates.first() ?: getFallbackRates()
        val rates = cached.rates
        
        val sourceRate = rates[sourceCurrency] ?: return 0.0
        val targetRate = rates[targetCurrency] ?: return 0.0
        
        return amount * (targetRate / sourceRate)
    }

    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).getSymbol(Locale.getDefault())
        } catch (e: Exception) {
            currencyCode
        }
    }

    fun getFallbackRates(): CachedRates {
        return CachedRates(
            lastUpdated = 0L,
            baseCurrency = "USD",
            rates = mapOf(
                "USD" to 1.0,
                "INR" to 83.92,
                "AED" to 3.6725,
                "EUR" to 0.898,
                "GBP" to 0.755,
                "JPY" to 144.35,
                "CAD" to 1.348,
                "AUD" to 1.472,
                "SGD" to 1.298,
                "CHF" to 0.846
            )
        )
    }
}
