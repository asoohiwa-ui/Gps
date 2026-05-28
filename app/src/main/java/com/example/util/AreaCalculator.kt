package com.example.util

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

object AreaCalculator {
    /**
     * Calculates the area of a closed polygon on Earth in square meters.
     */
    fun calculateSquareMeters(points: List<LatLng>): Double {
        if (points.size < 3) return 0.0
        return try {
            SphericalUtil.computeArea(points)
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Calculates the perimeter length of a path in meters. If [closed] is true,
     * includes the distance between the last and first points.
     */
    fun calculatePerimeterMeters(points: List<LatLng>, closed: Boolean = true): Double {
        if (points.size < 2) return 0.0
        return try {
            val length = SphericalUtil.computeLength(points)
            if (closed && points.size > 2) {
                val closingDist = SphericalUtil.computeDistanceBetween(points.last(), points.first())
                length + closingDist
            } else {
                length
            }
        } catch (e: Exception) {
            0.0
        }
    }

    fun formatArea(m2: Double): AreaFormatResult {
        return AreaFormatResult(
            sqMeters = m2,
            sqKilometers = m2 / 1_000_000.0,
            sqFeet = m2 * 10.7639104,
            acres = m2 * 0.000247105381,
            hectares = m2 * 0.0001
        )
    }

    fun formatPerimeter(m: Double): PerimeterFormatResult {
        return PerimeterFormatResult(
            meters = m,
            kilometers = m / 1000.0,
            feet = m * 3.2808399,
            miles = m * 0.000621371192
        )
    }
}

data class AreaFormatResult(
    val sqMeters: Double,
    val sqKilometers: Double,
    val sqFeet: Double,
    val acres: Double,
    val hectares: Double
) {
    fun getFormattedString(unit: MapUnit): String {
        return when (unit) {
            MapUnit.METERS -> String.format("%,.1f m²", sqMeters)
            MapUnit.KILOMETERS -> String.format("%,.4f km²", sqKilometers)
            MapUnit.FEET -> String.format("%,.1f ft²", sqFeet)
            MapUnit.ACRES -> String.format("%,.3f acres", acres)
            MapUnit.HECTARES -> String.format("%,.3f ha", hectares)
        }
    }
}

data class PerimeterFormatResult(
    val meters: Double,
    val kilometers: Double,
    val feet: Double,
    val miles: Double
) {
    fun getFormattedString(unit: MapUnit): String {
        return when (unit) {
            MapUnit.METERS -> String.format("%,.1f m", meters)
            MapUnit.KILOMETERS -> String.format("%,.3f km", kilometers)
            MapUnit.FEET -> String.format("%,.1f ft", feet)
            MapUnit.ACRES, MapUnit.HECTARES -> {
                // For acre/hectare layouts, express distance in meters or kilometers/miles
                if (meters >= 1000) String.format("%,.2f km", kilometers) else String.format("%,.1f m", meters)
            }
        }
    }
}

enum class MapUnit {
    METERS,
    KILOMETERS,
    FEET,
    ACRES,
    HECTARES;

    fun getDisplayName(): String {
        return when (this) {
            METERS -> "Sq Meters (m²)"
            KILOMETERS -> "Sq Kilometers (km²)"
            FEET -> "Sq Feet (ft²)"
            ACRES -> "Acres (ac)"
            HECTARES -> "Hectares (ha)"
        }
    }
}
