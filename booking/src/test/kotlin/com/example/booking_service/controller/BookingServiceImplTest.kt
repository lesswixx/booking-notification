package com.example.booking_service.controller

import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.kafka.AppointmentEventPublisher
import com.example.booking_service.model.Appointment
import com.example.booking_service.model.Doctor
import com.example.booking_service.model.Slot
import com.example.booking_service.model.Users
import com.example.booking_service.repository.AppointmentRepository
import com.example.booking_service.repository.DoctorRepository
import com.example.booking_service.repository.SlotRepository
import com.example.booking_service.repository.UserRepository
import com.example.booking_service.service.BookingServiceImpl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.never

import org.springframework.dao.DataIntegrityViolationException

import java.time.LocalDateTime
import java.util.Optional

internal class BookingServiceImplTest {

    private lateinit var userRepo: UserRepository
    private lateinit var doctorRepo: DoctorRepository
    private lateinit var slotRepo: SlotRepository
    private lateinit var apptRepo: AppointmentRepository
    private lateinit var eventPublisher: AppointmentEventPublisher
    private lateinit var service: BookingServiceImpl

    @BeforeEach
    fun setUp() {
        userRepo = mock(UserRepository::class.java)
        doctorRepo = mock(DoctorRepository::class.java)
        slotRepo = mock(SlotRepository::class.java)
        apptRepo = mock(AppointmentRepository::class.java)
        eventPublisher = mock(AppointmentEventPublisher::class.java)

        service = BookingServiceImpl(userRepo, doctorRepo, slotRepo, apptRepo, eventPublisher)
    }

    @Test
    fun `getAvailableSlots returns mapped list`() {
        val doctor = Doctor(id = 1, name = "Dr Strange", specialty = "Magic")
        val slotEntities = listOf(
            Slot(
                id = 10,
                dateTime = LocalDateTime.of(2025, 8, 10, 12, 0),
                booked = false,
                doctor = doctor
            )
        )
        `when`(slotRepo.findByDoctorIdAndBookedFalse(1L)).thenReturn(slotEntities)

        val slots: List<SlotDto> = service.getAvailableSlots(1L)
        assertEquals(1, slots.size)
        with(slots.first()) {
            assertEquals(10, id)
            assertEquals(1, doctorId)
            assertFalse(booked)
        }
    }


    @Test
    fun `bookAppointment throws when user not found`() {
        `when`(userRepo.findById(5L)).thenReturn(Optional.empty())

        val ex = assertThrows(ResourceNotFoundException::class.java) {
            service.bookAppointment(BookingRequestDto(userId = 5, doctorId = 1, slotId = 1))
        }
        assertTrue(ex.message!!.contains("User id=5 not found"))
    }

    @Test
    fun `bookAppointment throws when doctor not found`() {
        `when`(userRepo.findById(2L)).thenReturn(Optional.of(Users(2, "Bob", "bob@example.com")))
        `when`(doctorRepo.findById(7L)).thenReturn(Optional.empty())

        val ex = assertThrows(ResourceNotFoundException::class.java) {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 7, slotId = 4))
        }
        assertTrue(ex.message!!.contains("Doctor id=7 not found"))
    }

    @Test
    fun `bookAppointment throws when slot not found`() {
        `when`(userRepo.findById(2L)).thenReturn(Optional.of(Users(2, "Bob", "bob@example.com")))
        `when`(doctorRepo.findById(3L)).thenReturn(Optional.of(Doctor(3, "Dr", "Spec")))
        `when`(slotRepo.findById(8L)).thenReturn(Optional.empty())

        val ex = assertThrows(ResourceNotFoundException::class.java) {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 8))
        }
        assertTrue(ex.message!!.contains("Slot id=8 not found"))
    }

    @Test
    fun `bookAppointment throws when slot already booked`() {
        `when`(userRepo.findById(2L)).thenReturn(Optional.of(Users(2, "Bob", "bob@example.com")))
        `when`(doctorRepo.findById(3L)).thenReturn(Optional.of(Doctor(3, "Dr", "Spec")))
        val slot = Slot(
            id = 9,
            dateTime = LocalDateTime.now(),
            booked = true,
            doctor = doctorRepo.findById(3L).get()
        )
        `when`(slotRepo.findById(9L)).thenReturn(Optional.of(slot))

        assertThrows(SlotAlreadyBookedException::class.java) {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 9))
        }
        verify(apptRepo, never()).save(any(Appointment::class.java))
    }

    @Test
    fun `bookAppointment wraps DataIntegrityViolationException`() {
        val request = BookingRequestDto(userId = 2, doctorId = 3, slotId = 4)
        val user = Users(2, "A", "a@ex.com")
        val doctor = Doctor(3, "D", "S")
        val slot = Slot(
            id = 4,
            dateTime = LocalDateTime.now(),
            booked = false,
            doctor = doctor
        )
        `when`(userRepo.findById(2L)).thenReturn(Optional.of(user))
        `when`(doctorRepo.findById(3L)).thenReturn(Optional.of(doctor))
        `when`(slotRepo.findById(4L)).thenReturn(Optional.of(slot))
        `when`(apptRepo.save(any(Appointment::class.java)))
            .thenThrow(DataIntegrityViolationException("dup"))

        val ex = assertThrows(SlotAlreadyBookedException::class.java) {
            service.bookAppointment(request)
        }
        assertTrue(ex.message!!.contains("Slot id=4 already booked"))
    }


}
