data class WeatherAlert(
    val sender_name: String?, // Name of the weather service
    val event: String?,       // Event type (e.g., Flood Warning)
    val start: Long?,         // Start time of the alert (Unix timestamp)
    val end: Long?,           // End time of the alert (Unix timestamp)
    val description: String?, // Description of the event
    val tags: List<String>?   // Tags related to the alert (e.g., "Flood", "Warning")
)