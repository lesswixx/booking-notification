package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import com.example.notification_service.repository.AppointmentRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NotificationInitializer(
    private val appointmentRepo: AppointmentRepository,
    private val scheduler: NotificationScheduler
) {
    private val logger = LoggerFactory.getLogger(NotificationInitializer::class.java)

    @PostConstruct
    fun init() {
        val now = LocalDateTime.now()
        val upcoming = appointmentRepo.findByDateTimeAfter(now)
        logger.info("NotificationInitializer: найдено {} приёмов после {}", upcoming.size, now)

        upcoming.forEach { appt ->
            val event = AppointmentCreatedEvent(
                appointmentId = appt.id,
                userId        = appt.userId,
                doctorId      = appt.doctorId,
                dateTime      = appt.dateTime
            )
            logger.info("NotificationInitializer: планирую напоминания для appointmentId={}", appt.id)
            scheduler.scheduleReminders(event)
        }
    }
}
