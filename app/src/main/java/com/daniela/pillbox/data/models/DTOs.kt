package com.daniela.pillbox.data.models

import java.io.Serializable

data class Medication(
    val id: String,  // Changed to String (UUID or document ID)
    val userId: String,
    val name: String,
    val dosage: String,  // e.g. "500mg"
    val dosageUnit: String,  // e.g. "mg", "ml", "tablet"
    val type: String,  // e.g. "tablet", "capsule", "injection", "liquid"
    val schedule: Schedule,
    val instructions: String,
    val iconName: String? = null,
    val isActive: Boolean = true,
    val startDate: String? = null,  // ISO format "2023-12-31"
    val endDate: String? = null,
    val notes: String? = null,
    val stock: Int? = null,  // remaining quantity
    val sideEffects: List<String>? = null,
    val color: String? = null,  // For pill identification
    val shape: String? = null,  // For pill identification
    // val refillInfo: RefillInfo?  // Pharmacy contacts, prescription info
) : Serializable

data class Schedule(
    val timesPerDay: Int? = null,  // e.g. 2 (twice daily)
    val specificTimes: List<String>? = null,  // e.g. ["08:00", "20:00"]
    val daysOfWeek: List<Int>? = null,  // 1-7 (Monday-Sunday)
    val intervalHours: Int? = null,  // e.g. 12 (every 12 hours)
    val asNeeded: Boolean = false,  // PRN medications
) : Serializable