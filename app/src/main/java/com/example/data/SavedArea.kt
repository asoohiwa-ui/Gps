package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "saved_areas")
data class SavedArea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String, // e.g. Farm, Real Estate, Backyard, Wood, Other
    val areaSquareMeters: Double, // This can also store overall path meters depending on the mode
    val coordinatesString: String, // format: "lat,lng;lat,lng;lat,lng"
    val mode: String = "Area", // "Area", "Distance", "Walking"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getLatLngList(): List<LatLng> {
        if (coordinatesString.isBlank()) return emptyList()
        return try {
            coordinatesString.split(";")
                .filter { it.isNotBlank() }
                .map {
                    val parts = it.split(",")
                    LatLng(parts[0].toDouble(), parts[1].toDouble())
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromLatLngList(
            name: String,
            description: String,
            category: String,
            areaSquareMeters: Double,
            points: List<LatLng>,
            mode: String = "Area"
        ): SavedArea {
            val coordsStr = points.joinToString(";") { "${it.latitude},${it.longitude}" }
            return SavedArea(
                name = name,
                description = description,
                category = category,
                areaSquareMeters = areaSquareMeters,
                coordinatesString = coordsStr,
                mode = mode
            )
        }
    }
}
