import kotlinx.serialization.decodeFromString
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceInfo(
    @SerialName("NOK_per_kWh") val nokPerKWh: Double,
    @SerialName("EUR_per_kWh") val eurPerkWh: Double,
    @SerialName("time_start") val time_start: String
)

fun main() {
    try {
        val areas = mapOf("Oslo" to "NO1", "Kristiansand" to "NO2", "Trondheim" to "NO3", "Tromsø" to "NO4", "Bergen" to "NO5")
        var input: String? = null
        var kwh: Int? = null
        while (true) {
            println("Enter your area: ")
            input = readLine()
            if (input in areas) {
                break
            } else {
                println("Area not valid. Valid areas are: ${areas.keys.joinToString(", ")}.")
            }
        }
        while (true) {
            println("Amount of kWh: ")
            kwh = readLine()?.toIntOrNull()
            if (kwh != null) {
                break
            } else {
                println("kWh not valid.")
            }
        }

        val areaCode = areas[input]
        val url = URL("https://www.hvakosterstrommen.no/api/v1/prices/2024/03-06_$areaCode.json ")

        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connect()

        val responseCode = conn.responseCode

        if (responseCode != 200) {
            throw RuntimeException("HttpResponseCode: $responseCode")
        } else {
            val inline = Scanner(url.openStream()).useDelimiter("\\A").next()
            val json = Json { ignoreUnknownKeys = true }
            val priceList = json.decodeFromString<List<PriceInfo>>(inline)

            val NOKcost = priceList[0].nokPerKWh * (kwh?.toDouble() ?: 0.0)
            val EURcost = priceList[0].eurPerkWh * (kwh?.toDouble() ?: 0.0)
            val date = priceList[0].time_start
            println("As of $date, the price for $kwh kWh is: $NOKcost NOK, or $EURcost EUR.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
