package com.example.booking_service.service

import com.example.booking_service.model.Appointment
import com.example.booking_service.model.Doctor
import com.example.booking_service.model.Slot
import com.example.booking_service.model.Users
import java.time.LocalDateTime

interface AppointmentService {
  fun create(user: Users, doctor: Doctor, slot: Slot, dateTime: LocalDateTime): Appointment
  fun getActiveAppointments(): List<Appointment>
}