package com.example.booking_service.common

import java.time.LocalDateTime

data class AppointmentCreatedEvent(
    val appointmentId: Long,
    val userId:         Long,
    val doctorId:       Long,
    val dateTime:       LocalDateTime
)
