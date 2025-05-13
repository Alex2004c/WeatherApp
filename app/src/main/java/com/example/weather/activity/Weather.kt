package com.example.weather.activity

enum class WindSpeed {
    NONE, MEDIUM, STRONG
}

enum class Rain {
    NONE, LIGHT, HEAVY
}

enum class Cloudiness {
    CLEAR, PARTLY_CLOUDY, CLOUDY
}

enum class Temperature {
    Cold, Warm, Hot
}

enum class Sun {
    NONE, PRESENT, STRONG
}

class Weather(
    var windSpeed: WindSpeed,
    var temperatureDegress: Int,
    var temperature: Temperature,
    var ultrafiolet: Int,
    var chancePrecipitation : Double,
    var windSpeedNumber: Double,
    var flag_wind: Boolean,
    var flag_sun: Boolean,
    var flag_rain: Boolean,
    var rain: Rain,
    var cloudiness: Cloudiness,
    var sun: Sun
) {

    fun getWindSpeedCategory() {
        this.windSpeed = when {
            this.windSpeedNumber == 0.0 -> WindSpeed.NONE
            this.windSpeedNumber in 1.0..20.0 -> WindSpeed.MEDIUM
            this.windSpeedNumber > 20.0 -> WindSpeed.STRONG
            else -> WindSpeed.NONE
        }
    }

    fun getWindDescription(): String {
        val base = when (this.windSpeed) {
            WindSpeed.NONE -> "Погода спокойная, ветра нет."
            WindSpeed.MEDIUM -> "На улице умеренный ветер. Возможны лёгкие порывы. Советуем одеться потеплее."
            WindSpeed.STRONG -> "Сильный ветер! Будьте осторожны на улице."
        }
        return "Скорость ветра: ${String.format("%.1f", windSpeedNumber)} м/с. \n$base"
    }

    fun getSunDescription(): String {
        val base = when (this.sun) {
            Sun.NONE -> "Солнца сегодня не видно, возможна пасмурная погода."
            Sun.PRESENT -> "Солнечно, но не слишком жарко — отличная погода для прогулки."
            Sun.STRONG -> "Очень солнечно! Рекомендуется надеть головной убор и использовать солнцезащитный крем."
        }
        return "Уроветь ультрафиолета: ${ultrafiolet}. \n$base"
    }

    fun getCloudinessDescription(): String {
        return when (this.cloudiness) {
            Cloudiness.CLEAR -> "Небо чистое и ясное, без облаков."
            Cloudiness.PARTLY_CLOUDY -> "Частично облачно. Солнце временами скрывается за облаками."
            Cloudiness.CLOUDY -> "Небо затянуто облаками. Возможны осадки или просто пасмурная погода."
        }
    }

    fun getSunCategoryByValue(uvIndex: Double) {
        this.sun = when {
            uvIndex <= 1.0 -> Sun.NONE
            uvIndex in 1.1..5.0 -> Sun.PRESENT
            uvIndex > 5.0 -> Sun.STRONG
            else -> Sun.NONE
        }
    }

    fun getCloudinessCategoryByValue(cloudPercent: Int) {
        this.cloudiness = when {
            cloudPercent <= 10 -> Cloudiness.CLEAR
            cloudPercent in 11..50 -> Cloudiness.PARTLY_CLOUDY
            cloudPercent > 50 -> Cloudiness.CLOUDY
            else -> Cloudiness.CLEAR
        }
    }

    fun getRainDescription(): String {
        var base = when (this.rain) {
            Rain.NONE -> "Осадков не ожидается. Отличная возможность прогуляться."
            Rain.LIGHT -> "Лёгкий дождь. Возьмите с собой зонт на всякий случай."
            Rain.HEAVY -> "Сильный дождь! Лучше остаться в помещении."
        }

        return "Возможность выпадения осадков: ${chancePrecipitation}%. \n$base"
    }

    fun getRainCategoryByValue(precipitationMm: Double) {
        this.rain = when {
            precipitationMm == 0.0 -> Rain.NONE
            precipitationMm in 0.1..5.0 -> Rain.LIGHT
            precipitationMm > 5.0 -> Rain.HEAVY
            else -> Rain.NONE
        }
    }

    fun getTemperatureCategoryByValue(temp: Int) {
        this.temperatureDegress = temp
        this.temperature = when {
            temp < 10 -> Temperature.Cold
            temp in 10..24 -> Temperature.Warm
            temp >= 25 -> Temperature.Hot
            else -> Temperature.Warm
        }
    }

    fun getCloudsDescription(): String {
        return when (this.temperature) {
            Temperature.Cold -> "На улице холодно. Одевайтесь потеплее."
            Temperature.Warm -> "Теплая и приятная погода. Отличный день для прогулок."
            Temperature.Hot -> "Очень жарко. Рекомендуется пить больше воды и избегать длительного пребывания на солнце."
        }
    }

    fun getCloudsCategoryByValue(temp: Int) {
        this.temperatureDegress = temp
        this.temperature = when {
            temp < 10 -> Temperature.Cold
            temp in 10..24 -> Temperature.Warm
            temp >= 25 -> Temperature.Hot
            else -> Temperature.Warm
        }
    }

    fun getTemperatureDescription(): String {
        return when (this.temperature) {
            Temperature.Cold -> "На улице холодно. Одевайтесь потеплее."
            Temperature.Warm -> "Теплая и приятная погода. Отличный день для прогулок."
            Temperature.Hot -> "Очень жарко. Рекомендуется пить больше воды и избегать длительного пребывания на солнце."
        }
    }

    fun getWeatherSummaryAndAdvice(): Pair<String, String> {
        val advice = buildString {

            // Температура
            append("Температура: $temperatureDegress°C. ")
            when (temperature) {
                Temperature.Cold -> append("Холодно — наденьте тёплую одежду: шапку, перчатки, утеплённую куртку. Возможны переохлаждение и дискомфорт на открытом воздухе. ")
                Temperature.Warm -> append("Тепло и комфортно. Подойдут джинсы, лёгкая куртка или свитер. Отличная погода для прогулок. ")
                Temperature.Hot -> append("Очень жарко — лёгкая одежда обязательна. Избегайте длительного пребывания на солнце, особенно в полдень. ")
            }

            // Солнце
            append("\nУльтрафиолетовый индекс: $ultrafiolet. ")
            when (sun) {
                Sun.STRONG -> append("Высокая солнечная активность — используйте солнцезащитный крем, наденьте очки и головной убор. ")
                Sun.PRESENT -> append("Солнечно — комфортно для прогулок, можно загорать. ")
                Sun.NONE -> append("Солнце не активно — пасмурно или плотная облачность. ")
            }

            // Дождь
            append("\nВероятность осадков: ${String.format("%.0f", chancePrecipitation)}. ")
            when (rain) {
                Rain.LIGHT -> append("Лёгкий дождь — лучше взять зонт или водоотталкивающую куртку. ")
                Rain.HEAVY -> append("Сильный дождь — рекомендуется остаться дома или надеть непромокаемую одежду. ")
                Rain.NONE -> append("Осадков не ожидается. ")
            }

            // Ветер
            append("\nСкорость ветра: ${String.format("%.1f", windSpeedNumber)} м/с. ")
            when (windSpeed) {
                WindSpeed.NONE -> append("Ветра нет — комфортно на улице. ")
                WindSpeed.MEDIUM -> append("Умеренный ветер — может ощущаться прохлада, особенно в тенистых местах. ")
                WindSpeed.STRONG -> append("Сильный ветер — держите головные уборы крепче, избегайте открытых мест. ")
            }

            // Облачность
            append("\nОблачность: ")
            append(
                when (cloudiness) {
                    Cloudiness.CLEAR -> "Небо ясное. Отличный вид и настроение. "
                    Cloudiness.PARTLY_CLOUDY -> "Переменная облачность — возможны солнечные прояснения. "
                    Cloudiness.CLOUDY -> "Пасмурно — возможно снижение настроения, но без дождя. "
                }
            )

            // Общий совет
            append("\n\nРекомендуется одеваться по погоде, планировать прогулки с учётом осадков и солнца. Берите зонт и головной убор при необходимости.")
        }

        return "" to advice
    }



}
