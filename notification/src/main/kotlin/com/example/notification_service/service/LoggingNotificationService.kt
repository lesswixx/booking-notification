package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LoggingNotificationService : NotificationService {
    private val log = LoggerFactory.getLogger(LoggingNotificationService::class.java)

    override fun sendNotification(
        event: AppointmentCreatedEvent,
        reminderTime: LocalDateTime,
        type: ReminderType
    ) {
        log.info("[Напоминание {}] appointmentId={}, userId={}, doctorId={}, visitAt={}",
            type, event.appointmentId, event.userId, event.doctorId, event.dateTime
        )
    }
}
