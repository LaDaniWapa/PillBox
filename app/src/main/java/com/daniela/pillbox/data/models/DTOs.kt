package com.daniela.pillbox.data.models

data class Medication(
    val name: String,
    val dosage: String,
    val time: String,
    val instructions: String,
    val iconName: String? = null // "heart", "pill", "syringe" etc.
)