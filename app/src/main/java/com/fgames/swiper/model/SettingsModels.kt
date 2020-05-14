package com.fgames.swiper.model

class SettingsModel(
    val size: FieldSize,
    val intensity: MixIntensity
)

enum class MixIntensity(val value: Int) {
    LOW(5), MEDIUM(10), HIGH(30)
}

enum class FieldSize(val value: Int) {
    S3x3(3), S5x5(5), S7x7(7)
}