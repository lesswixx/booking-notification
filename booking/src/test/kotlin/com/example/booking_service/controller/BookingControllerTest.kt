package com.example.booking_service.controller

import com.example.booking_service.DTO.AppointmentDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.service.BookingServicee
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.internal.util.collections.CollectionHelper.listOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

internal class BookingControllerTest {

    private lateinit var service: BookingServicee
    private lateinit var mvc: MockMvc
    private val mapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        service = mock(BookingServicee::class.java)
        val controller = BookingController(service)
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(RestExceptionHandler())
            .build()
    }

    @Test
    fun `GET slots returns JSON list`() {
        val slots = listOf(
            SlotDto(
                id = 1,
                doctorId = 2,
                dateTime = LocalDateTime.of(2025, 8, 12, 8, 0),
                booked = false
            )
        )
        `when`(service.getAvailableSlots(2)).thenReturn(slots)

        mvc.perform(get("/api/bookings/slots/2"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
    }

    @Test
    fun `POST book returns appointment`() {
        val req = BookingRequestDto(userId = 5, doctorId = 6, slotId = 7)
        val resp = AppointmentDto(
            id = 9,
            userId = 5,
            doctorId = 6,
            slotId = 7,
            dateTime = LocalDateTime.of(2025, 8, 13, 9, 0)
        )
        `when`(service.bookAppointment(req)).thenReturn(resp)

        mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(9))
    }

    @Test
    fun `POST book 404 on ResourceNotFound`() {
        val req = BookingRequestDto(userId = 1, doctorId = 1, slotId = 1)
        `when`(service.bookAppointment(req))
            .thenThrow(ResourceNotFoundException("no user"))

        mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("no user"))
    }

    @Test
    fun `POST book 409 on SlotAlreadyBooked`() {
        val req = BookingRequestDto(userId = 1, doctorId = 1, slotId = 1)
        `when`(service.bookAppointment(req))
            .thenThrow(SlotAlreadyBookedException("busy"))

        mvc.perform(
            post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("busy"))
    }
}
