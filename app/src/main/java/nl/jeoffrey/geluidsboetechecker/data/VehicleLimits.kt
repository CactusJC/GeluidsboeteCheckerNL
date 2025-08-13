package nl.jeoffrey.geluidsboetechecker.data

object VehicleLimits {

    private val limits = mapOf(
        "Auto" to (70.0 to 90.0),
        "Motor" to (75.0 to 95.0),
        "Brommer" to (72.0 to 92.0)
    )

    private val defaultLimits = 70.0 to 90.0

    fun getLimitsFor(vehicle: String): Pair<Double, Double> {
        return limits.getOrDefault(vehicle, defaultLimits)
    }
}
