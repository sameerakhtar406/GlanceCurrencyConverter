package com.currencyconverter.data.api

import com.currencyconverter.data.model.ExchangeRateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ExchangeRateApi(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }
) {
    suspend fun getLatestRates(baseCurrency: String = "USD"): ExchangeRateResponse {
        return client.get("https://open.er-api.com/v6/latest/$baseCurrency").body()
    }
}
