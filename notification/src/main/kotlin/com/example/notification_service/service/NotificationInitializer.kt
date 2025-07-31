package com.example.notification_service.service

import com.example.notification_service.kafka.AppointmentEventListener  // для логики, не обязателен
import com.example.notification_service.model.AppointmentCreatedEvent
import com.example.notification_service.repository.AppointmentRepository
import com.example.notification_service.service.NotificationScheduler
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotificationInitializer(
    private val appointmentRepo: AppointmentRepository,
    private val scheduler: NotificationScheduler
) {

    private val log = LoggerFactory.getLogger(NotificationInitializer::class.java)

    @PostConstruct
    fun init() {
        val now = LocalDateTime.now()
        val upcoming = appointmentRepo.findByDateTimeAfter(now)
        log.info("NotificationInitializer: найдено {} приёмов после {}", upcoming.size, now)
        upcoming.forEach { appt ->
            val event = AppointmentCreatedEvent(
                appointmentId = appt.id,
                userId        = appt.userId,
                doctorId      = appt.doctorId,
                dateTime      = appt.dateTime
            )
            log.info("NotificationInitializer: планирую напоминания для appointmentId={}", appt.id)
            scheduler.scheduleReminders(event)
        }
    }
}
