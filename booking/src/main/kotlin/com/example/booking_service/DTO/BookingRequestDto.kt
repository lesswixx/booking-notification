package com.example.booking_service.DTO

data class BookingRequestDto(
    val userId: Long,
    val doctorId: Long,
    val slotId: Long
)
