package com.example.booking_service.service

import com.example.booking_service.DTO.AppointmentDto
import com.example.booking_service.DTO.BookingRequestDto
import com.example.booking_service.DTO.SlotDto
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.common.AppointmentCreatedEvent
import com.example.booking_service.kafka.AppointmentEventPublisher
import com.example.booking_service.model.Appointment
import com.example.booking_service.repository.AppointmentRepository
import com.example.booking_service.repository.DoctorRepository
import com.example.booking_service.repository.SlotRepository
import com.example.booking_service.repository.UserRepository
import com.example.booking_service.service.BookingServicee
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.*


@Service
class BookingServiceImpl(
    private val userRepo: UserRepository,
    private val doctorRepo: DoctorRepository,
    private val slotRepo: SlotRepository,
    private val apptRepo: AppointmentRepository,
    private val eventPublisher: AppointmentEventPublisher
) : BookingServicee {

    override fun getAvailableSlots(doctorId: Long): List<SlotDto> =
        slotRepo.findByDoctorIdAndBookedFalse(doctorId)
            .map { slot ->
                SlotDto(
                    id        = slot.id,
                    doctorId  = slot.doctor.id,
                    dateTime  = slot.dateTime,
                    booked    = slot.booked
                )
            }


    @Transactional("transactionManager")
    override fun bookAppointment(request: BookingRequestDto): AppointmentDto {
        val user = userRepo.findById(request.userId)
            .orElseThrow { ResourceNotFoundException("User id=${request.userId} not found") }
        val doctor = doctorRepo.findById(request.doctorId)
            .orElseThrow { ResourceNotFoundException("Doctor id=${request.doctorId} not found") }
        val slot = slotRepo.findById(request.slotId)
            .orElseThrow { ResourceNotFoundException("Slot id=${request.slotId} not found") }

        if (slot.booked) {
            throw SlotAlreadyBookedException("Slot id=${slot.id} already booked")
        }
        slot.booked = true
        slotRepo.save(slot)

        val apptEntity = Appointment(
            users    = user,
            doctor   = doctor,
            slot     = slot,
            dateTime = slot.dateTime
        )
        val saved = try {
            apptRepo.save(apptEntity)
        } catch (ex: DataIntegrityViolationException) {
            throw SlotAlreadyBookedException("Slot id=${slot.id} already booked")
        }

        val event = AppointmentCreatedEvent(
            appointmentId = saved.id,
            userId        = saved.users.id,
            doctorId      = saved.doctor.id,
            dateTime      = saved.dateTime
        )
        eventPublisher.publishAfterCommit(event)

        return AppointmentDto(
            id        = saved.id,
            userId    = saved.users.id,
            doctorId  = saved.doctor.id,
            slotId    = saved.slot.id,
            dateTime  = saved.dateTime
        )
    }


    override fun getActiveAppointments(): List<AppointmentDto> {
        val now = java.time.LocalDateTime.now()
        return apptRepo.findByDateTimeAfter(now)
            .map { appt ->
                AppointmentDto(
                    id        = appt.id,
                    userId    = appt.users.id,
                    doctorId  = appt.doctor.id,
                    slotId    = appt.slot.id,
                    dateTime  = appt.dateTime
                )
            }
    }
}
