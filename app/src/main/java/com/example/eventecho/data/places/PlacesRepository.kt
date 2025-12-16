package com.example.eventecho.data.places

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.suspendCoroutine
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.resume

// used to get autocomplete predictions from Google Places API
data class PlaceSuggestion(
    val placeId: String,
    val description: String,
    val lat: Double,
    val lng: Double
)

class PlacesRepository(context: Context) {

    private val placesClient = Places.createClient(context)

    suspend fun autocomplete(query: String): List<PlaceSuggestion> =
        suspendCoroutine { continuation ->

            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->

                    // For each prediction, fetch PLACE DETAILS to get lat/lng
                    val tasks = response.autocompletePredictions.map { prediction ->
                        val placeId = prediction.placeId

                        val detailsRequest = FetchPlaceRequest
                            .builder(placeId, listOf(Place.Field.LAT_LNG))
                            .build()

                        placesClient.fetchPlace(detailsRequest)
                            .continueWithTask { task ->
                                val result = task.result
                                val latLng = result?.place?.latLng

                                Tasks.forResult(
                                    PlaceSuggestion(
                                        placeId = placeId,
                                        description = prediction.getFullText(null).toString(),
                                        lat = latLng?.latitude ?: 0.0,
                                        lng = latLng?.longitude ?: 0.0
                                    )
                                )
                            }
                    }

                    Tasks.whenAllSuccess<PlaceSuggestion>(tasks)
                        .addOnSuccessListener { list ->
                            continuation.resume(list)
                        }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(emptyList())
                }
        }
}
