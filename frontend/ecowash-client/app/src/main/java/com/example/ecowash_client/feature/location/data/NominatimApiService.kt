package com.example.ecowash_client.feature.location.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countryCodes: String = "pe",
        @Query("addressdetails") addressDetails: Int = 1
    ): List<NominatimResult>
}

data class NominatimResult(
    val display_name: String,
    val lat: String,
    val lon: String,
    val type: String? = null,
    val importance: Double? = null
)
