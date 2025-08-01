package com.example.booking_service.DTO

import java.time.LocalDateTime

data class SlotDto(
    val id: Long,
    val doctorId: Long,
    val dateTime: LocalDateTime,
    val booked: Boolean
)
