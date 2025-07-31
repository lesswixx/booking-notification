// src/main/kotlin/com/example/bookingservice/service/BookingService.kt
package com.example.booking_service.service

import com.example.booking_service.DTO.AppointmentDto
import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.DTO.SlotDto


interface BookingServicee {
    fun getAvailableSlots(doctorId: Long): List<SlotDto>
    fun bookAppointment(request: BookingRequestDto): AppointmentDto
    fun getActiveAppointments(): List<AppointmentDto>
}
