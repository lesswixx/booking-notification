package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import java.time.LocalDateTime

interface NotificationService {
    fun sendNotification(
        event: AppointmentCreatedEvent,
        reminderTime: LocalDateTime,
        type: ReminderType
    )
}