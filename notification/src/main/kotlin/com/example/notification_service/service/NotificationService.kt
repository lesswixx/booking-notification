package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import java.time.LocalDateTime

/**
 * Интерфейс для отправки уведомлений.
 * точка расширения для SMS, Email и т.д.
 */
interface NotificationService {
    fun sendNotification(
        event: AppointmentCreatedEvent,
        reminderTime: LocalDateTime,
        type: ReminderType
    )
}