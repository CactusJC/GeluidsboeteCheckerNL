package nl.jeoffrey.geluidsboetechecker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleLimitsTest {

    @Test
    fun `getLimitsFor car returns correct limits`() {
        val limits = VehicleLimits.getLimitsFor(VehicleType.CAR)
        assertEquals(70 to 90, limits)
    }

    @Test
    fun `getLimitsFor motorcycle returns correct limits`() {
        val limits = VehicleLimits.getLimitsFor(VehicleType.MOTORCYCLE)
        assertEquals(75 to 95, limits)
    }

    @Test
    fun `getLimitsFor moped returns correct limits`() {
        val limits = VehicleLimits.getLimitsFor(VehicleType.MOPED)
        assertEquals(72 to 92, limits)
    }
}
