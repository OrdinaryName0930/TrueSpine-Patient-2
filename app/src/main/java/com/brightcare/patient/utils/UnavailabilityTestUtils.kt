package com.brightcare.patient.utils

import android.util.Log
import com.brightcare.patient.data.model.ChiropractorUnavailability

/**
 * Test utilities for debugging chiropractor unavailability
 * Utility para sa pag-test ng chiropractor unavailability
 */
object UnavailabilityTestUtils {
    
    /**
     * Test the unavailability parsing with sample data
     * I-test ang parsing ng unavailability gamit ang sample data
     */
    fun testUnavailabilityParsing() {
        Log.d("UnavailabilityTest", "=== Testing Unavailability Parsing ===")
        
        // Sample data from TrueSpine4.json
        val sampleData = mapOf(
            "h" to "h",
            "dates" to mapOf(
                "8edsaYk6LRjM8Se3HkcG" to mapOf(
                    "date" to "2025-12-10",
                    "fullDay" to true,
                    "times" to listOf("")
                ),
                "Su9lDeE3gNHVYHf3MaGk" to mapOf(
                    "date" to "2025-12-10",
                    "times" to listOf("10:00", "14:00"),
                    "fullDay" to false
                )
            )
        )
        
        val chiropractorId = "GHkvU5c8c4SZHqK63HwJ18TDvEZ2"
        val unavailability = ChiropractorUnavailability.fromMap(chiropractorId, sampleData)
        
        Log.d("UnavailabilityTest", "Parsed unavailability: $unavailability")
        Log.d("UnavailabilityTest", "Number of unavailable dates: ${unavailability.dates.size}")
        
        // Test specific date checking
        val testDate = "2025-12-10"
        val isFullyUnavailable = unavailability.isDateFullyUnavailable(testDate)
        Log.d("UnavailabilityTest", "Is $testDate fully unavailable? $isFullyUnavailable")
        
        // Test time checking
        val isTimeUnavailable10 = unavailability.isTimeUnavailable(testDate, "10:00")
        val isTimeUnavailable14 = unavailability.isTimeUnavailable(testDate, "14:00")
        val isTimeUnavailable16 = unavailability.isTimeUnavailable(testDate, "16:00")
        
        Log.d("UnavailabilityTest", "Is 10:00 unavailable on $testDate? $isTimeUnavailable10")
        Log.d("UnavailabilityTest", "Is 14:00 unavailable on $testDate? $isTimeUnavailable14")
        Log.d("UnavailabilityTest", "Is 16:00 unavailable on $testDate? $isTimeUnavailable16")
        
        Log.d("UnavailabilityTest", "=== Test Complete ===")
    }
}













