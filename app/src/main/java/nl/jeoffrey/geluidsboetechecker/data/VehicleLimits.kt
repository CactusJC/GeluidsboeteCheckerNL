package nl.jeoffrey.geluidsboetechecker.data

object VehicleLimits {

    private val limits = mapOf(
        VehicleType.CAR to (70 to 90),
        VehicleType.MOTORCYCLE to (75 to 95),
        VehicleType.MOPED to (72 to 92)
    )

    private val defaultLimits = 70 to 90

    fun getLimitsFor(vehicleType: VehicleType): Pair<Int, Int> {
        return limits.getOrDefault(vehicleType, defaultLimits)
    }
}
