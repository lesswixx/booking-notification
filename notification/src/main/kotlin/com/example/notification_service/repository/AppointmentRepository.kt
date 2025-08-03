package com.example.notification_service.repository

import com.example.notification_service.model.Appointment
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface AppointmentRepository : JpaRepository<Appointment, Long> {
    fun findByDateTimeAfter(dateTime: LocalDateTime): List<Appointment>
}
