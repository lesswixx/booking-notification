// src/main/kotlin/com/example/notification_service/service/NotificationScheduler.kt
package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Service
class NotificationScheduler(
    private val taskScheduler: TaskScheduler,
    private val notificationService: NotificationService
) {

    private val log = LoggerFactory.getLogger(NotificationScheduler::class.java)
    private val zone: ZoneId = ZoneId.systemDefault()

    /** Планируем два напоминания для визита */
    fun scheduleReminders(event: AppointmentCreatedEvent) {
        scheduleOne(ReminderType.DAY_BEFORE, event.dateTime.minusDays(1), event)
        scheduleOne(ReminderType.TWO_HOURS_BEFORE, event.dateTime.minusHours(2), event)
    }

    private fun scheduleOne(
        type: ReminderType,
        runAt: LocalDateTime,
        event: AppointmentCreatedEvent
    ) {
        val now = LocalDateTime.now()
        if (runAt.isBefore(now)) {
            log.info(
                "Пропускаю напоминание '{}': время уже прошло (runAt={}, now={})",
                type, runAt, now
            )
            return
        }

        val execTime = Date.from(runAt.atZone(zone).toInstant())
        val delaySec = Duration.between(now, runAt).toSeconds()

        log.info(
            "Планирую 'Напоминание {}' для appointmentId={}, runAt={}, через ~{} сек.",
            type, event.appointmentId, runAt, delaySec
        )

        taskScheduler.schedule({
            notificationService.sendNotification(event, runAt, type)
        }, execTime)
    }
}
