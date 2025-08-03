package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import com.example.notification_service.service.ReminderType.DAY_BEFORE
import com.example.notification_service.service.ReminderType.TWO_HOURS_BEFORE
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class NotificationScheduler(
    private val taskScheduler: TaskScheduler,
    private val notificationService: NotificationService
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)
    private val zone: ZoneId = ZoneId.systemDefault()

    fun scheduleReminders(event: AppointmentCreatedEvent) {
        scheduleOne(DAY_BEFORE, event.dateTime.minusDays(1), event)
        scheduleOne(TWO_HOURS_BEFORE, event.dateTime.minusHours(2), event)
    }

    private fun scheduleOne(
        type: ReminderType,
        runAt: LocalDateTime,
        event: AppointmentCreatedEvent
    ) {
        val now = LocalDateTime.now()
        if (runAt.isBefore(now)) {
            logger.info("Пропускаю напоминание '{}': runAt={}, now={}", type, runAt, now)
            return
        }

        val instant = runAt.atZone(zone).toInstant()
        val delaySec = Duration.between(now, runAt).toSeconds()
        logger.info(
            "Планирую напоминание '{}' для appointmentId={}, runAt={}, через ~{} сек.",
            type, event.appointmentId, runAt, delaySec
        )

        taskScheduler.schedule({
            notificationService.sendNotification(event, runAt, type)
        }, instant)
    }
}
