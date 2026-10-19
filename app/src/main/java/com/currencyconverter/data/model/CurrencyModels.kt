package com.currencyconverter.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val time_last_update_unix: Long,
    val time_last_update_utc: String,
    val rates: Map<String, Double>
)

@Serializable
data class CachedRates(
    val lastUpdated: Long,
    val baseCurrency: String,
    val rates: Map<String, Double>
)
