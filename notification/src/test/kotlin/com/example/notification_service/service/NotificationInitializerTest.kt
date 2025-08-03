package com.example.notification_service.service

import com.example.notification_service.model.Appointment
import com.example.notification_service.model.AppointmentCreatedEvent
import com.example.notification_service.repository.AppointmentRepository
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class NotificationInitializerTest {
    private val repo: AppointmentRepository = mock()
    private val scheduler: NotificationScheduler = mock()
    private lateinit var initializer: NotificationInitializer

    @BeforeEach
    fun setUp() {
        initializer = NotificationInitializer(repo, scheduler)
    }

    @Test
    fun `init schedules reminders for all future appointments`() {
        val now = LocalDateTime.now()
        val future1 = Appointment(
            id       = 1,
            userId   = 1,
            doctorId = 1,
            dateTime = now.plusDays(1)
        )
        val future2 = Appointment(
            id       = 2,
            userId   = 2,
            doctorId = 2,
            dateTime = now.plusHours(3)
        )
        whenever(repo.findByDateTimeAfter(any())).thenReturn(listOf(future1, future2))
        initializer.init()
        verify(scheduler).scheduleReminders(
            AppointmentCreatedEvent(
                appointmentId = future1.id,
                userId        = future1.userId,
                doctorId      = future1.doctorId,
                dateTime      = future1.dateTime
            )
        )
        verify(scheduler).scheduleReminders(
            AppointmentCreatedEvent(
                appointmentId = future2.id,
                userId        = future2.userId,
                doctorId      = future2.doctorId,
                dateTime      = future2.dateTime
            )
        )
    }
}
