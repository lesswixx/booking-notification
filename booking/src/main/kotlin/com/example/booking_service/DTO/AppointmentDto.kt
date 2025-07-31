// dto/AppointmentDto.kt
package com.example.booking_service.DTO

import java.time.LocalDateTime

data class AppointmentDto(
    val id: Long,
    val userId: Long,
    val doctorId: Long,
    val slotId: Long,
    val dateTime: LocalDateTime
)
