package com.example.booking_service.service

import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.common.AppointmentCreatedEvent
import com.example.booking_service.kafka.AppointmentEventPublisher
import com.example.booking_service.model.Appointment
import com.example.booking_service.model.Doctor
import com.example.booking_service.model.Slot
import com.example.booking_service.model.Users
import com.example.booking_service.repository.DoctorRepository
import com.example.booking_service.repository.UserRepository
import com.example.booking_service.service.AppointmentService
import com.example.booking_service.service.BookingServiceImpl
import com.example.booking_service.service.SlotService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class BookingServiceImplTest {

    private lateinit var userRepo: UserRepository
    private lateinit var doctorRepo: DoctorRepository
    private lateinit var slotService: SlotService
    private lateinit var appointmentService: AppointmentService
    private lateinit var eventPublisher: AppointmentEventPublisher
    private lateinit var service: BookingServiceImpl

    @BeforeEach
    fun setup() {
        userRepo = mock()
        doctorRepo = mock()
        slotService = mock()
        appointmentService = mock()
        eventPublisher = mock()

        service = BookingServiceImpl(
            userRepo,
            doctorRepo,
            slotService,
            appointmentService,
            eventPublisher
        )
    }

    @Test
    fun `getAvailableSlots returns mapped list`() {
        val doctor = Doctor(id = 1, name = "Dr Strange", specialty = "Magic")
        val slot = Slot(
            id = 10,
            doctor = doctor,
            dateTime = LocalDateTime.of(2025, 8, 10, 12, 0),
            booked = false
        )
        whenever(slotService.getAvailableSlots(1L)).thenReturn(listOf(slot))

        val result: List<SlotDto> = service.getAvailableSlots(1L)
        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(10, id)
            assertEquals(1, doctorId)
            assertEquals(slot.dateTime, dateTime)
            assertEquals(false, booked)
        }
        verify(slotService).getAvailableSlots(1L)
    }

    @Test
    fun `bookAppointment throws when user not found`() {
        whenever(userRepo.findById(5L)).thenReturn(java.util.Optional.empty())

        val ex = assertFailsWith<ResourceNotFoundException> {
            service.bookAppointment(BookingRequestDto(userId = 5, doctorId = 1, slotId = 1))
        }
        assertEquals("User id=5 not found", ex.message)
    }

    @Test
    fun `bookAppointment throws when doctor not found`() {
        val user = Users(id = 2, name = "Alice", email = "alice@example.com")
        whenever(userRepo.findById(2L)).thenReturn(java.util.Optional.of(user))
        whenever(doctorRepo.findById(7L)).thenReturn(java.util.Optional.empty())

        val ex = assertFailsWith<ResourceNotFoundException> {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 7, slotId = 3))
        }
        assertEquals("Doctor id=7 not found", ex.message)
    }

    @Test
    fun `bookAppointment throws when slot not found`() {
        val user = Users(id = 2, name = "Bob", email = "bob@example.com")
        val doctor = Doctor(id = 3, name = "Dr Who", specialty = "Time")
        whenever(userRepo.findById(2L)).thenReturn(java.util.Optional.of(user))
        whenever(doctorRepo.findById(3L)).thenReturn(java.util.Optional.of(doctor))
        whenever(slotService.bookSlot(8L)).thenThrow(ResourceNotFoundException("Slot id=8 not found"))

        val ex = assertFailsWith<ResourceNotFoundException> {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 8))
        }
        assertEquals("Slot id=8 not found", ex.message)
    }

    @Test
    fun `bookAppointment throws when slot already booked by slotService`() {
        val user = Users(id = 2, name = "Bob", email = "bob@example.com")
        val doctor = Doctor(id = 3, name = "Dr Who", specialty = "Time")
        whenever(userRepo.findById(2L)).thenReturn(java.util.Optional.of(user))
        whenever(doctorRepo.findById(3L)).thenReturn(java.util.Optional.of(doctor))
        whenever(slotService.bookSlot(9L)).thenThrow(SlotAlreadyBookedException("Slot id=9 already booked"))

        val ex = assertFailsWith<SlotAlreadyBookedException> {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 9))
        }
        assertEquals("Slot id=9 already booked", ex.message)
    }

    @Test
    fun `bookAppointment wraps DataIntegrityViolationException from appointmentService`() {
        val user = Users(id = 2, name = "A", email = "a@ex.com")
        val doctor = Doctor(id = 3, name = "D", specialty = "S")
        val slot = Slot(id = 4, doctor = doctor, dateTime = LocalDateTime.now(), booked = false)

        whenever(userRepo.findById(2L)).thenReturn(java.util.Optional.of(user))
        whenever(doctorRepo.findById(3L)).thenReturn(java.util.Optional.of(doctor))
        whenever(slotService.bookSlot(4L)).thenReturn(slot)
        whenever(appointmentService.create(any(), any(), any(), any()))
            .thenThrow(SlotAlreadyBookedException("Slot id=4 already booked"))

        val ex = assertFailsWith<SlotAlreadyBookedException> {
            service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 4))
        }
        assertEquals("Slot id=4 already booked", ex.message)
    }

    @Test
    fun `bookAppointment succeeds and publishes event`() {
        val user = Users(id = 2, name = "Carl", email = "c@ex.com")
        val doctor = Doctor(id = 3, name = "Doc", specialty = "Spec")
        val slot = Slot(id = 5, doctor = doctor, dateTime = LocalDateTime.of(2025,8,5,10,0), booked = false)
        val appt = Appointment(id = 100, users = user, doctor = doctor, slot = slot, dateTime = slot.dateTime)

        whenever(userRepo.findById(2L)).thenReturn(java.util.Optional.of(user))
        whenever(doctorRepo.findById(3L)).thenReturn(java.util.Optional.of(doctor))
        whenever(slotService.bookSlot(5L)).thenReturn(slot)
        whenever(appointmentService.create(user, doctor, slot, slot.dateTime)).thenReturn(appt)

        val result = service.bookAppointment(BookingRequestDto(userId = 2, doctorId = 3, slotId = 5))

        assertEquals(100, result.id)
        assertEquals(2, result.userId)
        assertEquals(3, result.doctorId)
        assertEquals(5, result.slotId)
        verify(eventPublisher).publishAfterCommit(
            AppointmentCreatedEvent(100, 2, 3, slot.dateTime)
        )
    }

    @Test
    fun `getActiveAppointments returns mapped list`() {
        val user = Users(id = 2, name = "Carl", email = "c@ex.com")
        val doctor = Doctor(id = 3, name = "Doc", specialty = "Spec")
        val slot = Slot(id = 5, doctor = doctor, dateTime = LocalDateTime.of(2025,8,5,10,0), booked = false)
        val appt = Appointment(id = 200, users = user, doctor = doctor, slot = slot, dateTime = slot.dateTime)

        whenever(appointmentService.getActiveAppointments()).thenReturn(listOf(appt))

        val list = service.getActiveAppointments()
        assertEquals(1, list.size)
        with(list.first()) {
            assertEquals(200, id)
            assertEquals(2, userId)
            assertEquals(3, doctorId)
            assertEquals(5, slotId)
        }
        verify(appointmentService).getActiveAppointments()
    }
}
