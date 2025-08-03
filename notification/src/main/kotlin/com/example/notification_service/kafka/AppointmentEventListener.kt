package com.example.notification_service.kafka

import com.example.notification_service.model.AppointmentCreatedEvent
import com.example.notification_service.service.NotificationScheduler
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class AppointmentEventListener(
    private val scheduler: NotificationScheduler
) {

    private val log = LoggerFactory.getLogger(AppointmentEventListener::class.java)

    @KafkaListener(
        topics = ["appointments.created"],
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun onMessage(
        @Payload event: AppointmentCreatedEvent,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        log.info(
            "Получено событие: topic={}, partition={}, offset={}, payload={}",
            topic, partition, offset, event
        )

        scheduler.scheduleReminders(event)
    }


}
