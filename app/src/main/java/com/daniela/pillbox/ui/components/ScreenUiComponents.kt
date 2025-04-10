package com.daniela.pillbox.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.daniela.pillbox.data.models.Medication

// TODO: redo this file. Merge this with the one in [StorageScreen]

/**
 * Represents an item in the medication list.
 */
@Composable
fun MedicationItem(
    medication: Medication,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        border = if (isChecked) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!isChecked) }
                .padding(vertical = 12.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = medication.schedule.specificTimes?.get(0) ?: "09:00",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Medication icon
            MedicationIcon(
                iconName = medication.iconName,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Medication details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${medication.dosage} • ${medication.instructions}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }

            // Checkbox
            Checkbox(
                checked = isChecked,
                onCheckedChange = null, // Handled by Row click
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
            )
        }
    }
}

/**
 * Displays an icon representing the medication type.
 */
@Composable
fun MedicationIcon(iconName: String?, modifier: Modifier = Modifier) {
    val icon: Pair<ImageVector, Color> = when (iconName?.lowercase()) {
        "heart" -> Icons.Default.MonitorHeart to Color(0xFFE57373)
        "pill" -> Icons.Default.Medication to Color(0xFF81C784)
        "syringe" -> Icons.Default.MedicalServices to Color(0xFF64B5F6)
        "capsule" -> Icons.Default.LocalPharmacy to Color(0xFFBA68C8)
        "eye" -> Icons.Default.RemoveRedEye to Color(0xFF4FC3F7)
        else -> Icons.Default.Medication to Color(0xFF7986CB)
    }

    Icon(
        imageVector = icon.first,
        contentDescription = "Medication type",
        tint = icon.second,
        modifier = modifier.size(25.dp)
    )
}
