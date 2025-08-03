package com.example.booking_service.controller

import com.example.booking_service.DTO.AppointmentDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.service.BookingServicee
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingService: BookingServicee
) {

    @GetMapping("/slots/{doctorId}")
    fun getAvailableSlots(@PathVariable doctorId: Long): List<SlotDto> =
        bookingService.getAvailableSlots(doctorId)

    @PostMapping
    fun bookAppointment(
        @RequestBody request: BookingRequestDto
    ): AppointmentDto =
        bookingService.bookAppointment(request)

    @GetMapping
    fun getActiveAppointments(): List<AppointmentDto> =
        bookingService.getActiveAppointments()
}
