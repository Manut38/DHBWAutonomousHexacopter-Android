package net.gyroinc.dhbwhexacopter.utils

import io.mockk.every
import io.mockk.mockk
import net.gyroinc.dhbwhexacopter.models.DroneGpsStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class LatLngConverterTest {

    @Test
    fun `converts LatLng Int to Double correctly`() {
        val value = LatLngConverter.toLatLngDouble(49000000)
        assertEquals(49.0, value)
    }

    @Test
    fun `converts LatLng Double to Int correctly`() {
        val value = LatLngConverter.toLatLngInt(49.0)
        assertEquals(490000000, value)
    }

    @Test
    fun `converts from DroneGpsStatus correctly`() {
        val gpsStatus: DroneGpsStatus = mockk()
        every {gpsStatus.lat} returns 49000000
        every {gpsStatus.lon} returns 8000000
        val latLng = LatLngConverter.fromDroneGpsStatus(gpsStatus)
        assertEquals(49.0, latLng.latitude)
        assertEquals(8.0, latLng.longitude)
    }
}
