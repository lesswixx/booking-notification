package com.example.notification_service.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class AppointmentCreatedEvent(
    val appointmentId: Long,
    val userId: Long,
    val doctorId: Long,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val dateTime: LocalDateTime
)
