package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LoggingNotificationService(
    private val logger: Logger = LoggerFactory.getLogger(LoggingNotificationService::class.java)
) : NotificationService {

    override fun sendNotification(
        event: AppointmentCreatedEvent,
        reminderTime: LocalDateTime,
        type: ReminderType
    ) {
        logger.info(
            "[Напоминание {}] appointmentId={}, userId={}, doctorId={}, visitAt={}",
            type, event.appointmentId, event.userId, event.doctorId, event.dateTime
        )
    }
}
