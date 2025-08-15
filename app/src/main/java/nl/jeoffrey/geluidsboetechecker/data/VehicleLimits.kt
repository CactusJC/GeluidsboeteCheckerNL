package nl.jeoffrey.geluidsboetechecker.data

object VehicleLimits {

    private val limits = mapOf(
        "Auto" to (70 to 90),
        "Motor" to (75 to 95),
        "Brommer" to (72 to 92)
    )

    private val defaultLimits = 70 to 90

    fun getLimitsFor(vehicle: String): Pair<Int, Int> {
        return limits.getOrDefault(vehicle, defaultLimits)
    }
}
