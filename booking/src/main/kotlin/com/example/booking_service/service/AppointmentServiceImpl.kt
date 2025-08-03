package com.example.booking_service.service

import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.model.Appointment
import com.example.booking_service.model.Doctor
import com.example.booking_service.model.Slot
import com.example.booking_service.model.Users
import com.example.booking_service.repository.AppointmentRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalDateTime.now

@Service
class AppointmentServiceImpl(
  private val appointmentRepository: AppointmentRepository,
) : AppointmentService {
  override fun create(user: Users, doctor: Doctor, slot: Slot, dateTime: LocalDateTime): Appointment {
    return  try {
      appointmentRepository.save(
        Appointment(
          users = user,
          doctor = doctor,
          slot = slot,
          dateTime = slot.dateTime
        )
      )
    } catch (ex: DataIntegrityViolationException) {
      throw SlotAlreadyBookedException("Slot id=${slot.id} already booked")
    }
  }

  override fun getActiveAppointments(): List<Appointment> =
    appointmentRepository.findByDateTimeAfter(now())
}