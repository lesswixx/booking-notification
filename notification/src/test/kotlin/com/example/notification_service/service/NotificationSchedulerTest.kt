package com.example.notification_service.service

import com.example.notification_service.model.AppointmentCreatedEvent
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.scheduling.TaskScheduler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

internal class NotificationSchedulerTest {

    private val taskScheduler: TaskScheduler = mock()
    private val notifier: NotificationService = mock()
    private val scheduler = NotificationScheduler(taskScheduler, notifier)
    private val zone = ZoneId.systemDefault()

    @Test
    fun scheduleRemindersSchedulesTwoInstants() {
        val dt = LocalDateTime.of(2025, 8, 5, 10, 0)
        val event = AppointmentCreatedEvent(1, 2, 3, dt)

        scheduler.scheduleReminders(event)

        val captor = argumentCaptor<Instant>()
        verify(taskScheduler, times(2))
            .schedule(any<Runnable>(), captor.capture())

        val actual = captor.allValues.toSet()
        val expected = setOf(
            dt.minusDays(1).atZone(zone).toInstant(),
            dt.minusHours(2).atZone(zone).toInstant()
        )

        assertEquals(expected, actual)
    }

    @Test
    fun scheduleRemindersSkipsPast() {
        val now = LocalDateTime.now()
        val event = AppointmentCreatedEvent(1, 2, 3, now.plusHours(1))

        scheduler.scheduleReminders(event)

        verify(taskScheduler, times(0))
            .schedule(any<Runnable>(), any<Instant>())
    }
}
