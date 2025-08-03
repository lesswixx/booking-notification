package com.example.booking_service.service

import com.example.booking_service.DTO.AppointmentDto
import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.common.AppointmentCreatedEvent
import com.example.booking_service.kafka.AppointmentEventPublisher
import com.example.booking_service.repository.DoctorRepository
import com.example.booking_service.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
@Service
class BookingServiceImpl(
    private val userRepo: UserRepository,
    private val doctorRepo: DoctorRepository,
    private val slotService: SlotService,
    private val appointmentService: AppointmentService,
    private val eventPublisher: AppointmentEventPublisher
): BookingServicee {

    override fun getAvailableSlots(doctorId: Long): List<SlotDto> =
        slotService.getAvailableSlots(doctorId).map { slot ->
            SlotDto(slot.id!!, slot.doctor.id!!, slot.dateTime, slot.booked)
        }

    @Transactional("transactionManager")
    override fun bookAppointment(request: BookingRequestDto): AppointmentDto {
        val user = userRepo.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User id=${request.userId} not found") }
        val doctor = doctorRepo.findById(request.doctorId)
            .orElseThrow { ResourceNotFoundException("Doctor id=${request.doctorId} not found") }

        val slot = slotService.bookSlot(request.slotId)
        val appt = appointmentService.create(user, doctor, slot, slot.dateTime)

        eventPublisher.publishAfterCommit(
            AppointmentCreatedEvent(appt.id!!, user.id!!, doctor.id!!, appt.dateTime)
        )

        return AppointmentDto(appt.id, user.id, doctor.id, slot.id!!, appt.dateTime)
    }

    override fun getActiveAppointments(): List<AppointmentDto> =
        appointmentService.getActiveAppointments().map { appt ->
            AppointmentDto(appt.id!!, appt.users.id!!, appt.doctor.id!!, appt.slot.id!!, appt.dateTime)
        }
}

