package com.example.booking_service.kafka

import com.example.booking_service.common.AppointmentCreatedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class AppointmentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, AppointmentCreatedEvent>
) {
    companion object {
        const val TOPIC = "appointments.created"
    }

    fun publishAfterCommit(event: AppointmentCreatedEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        kafkaTemplate.executeInTransaction { producer ->
                            producer.send(TOPIC, event).get()
                        }
                    }
                }
            )
        } else {
            kafkaTemplate.executeInTransaction { producer ->
                producer.send(TOPIC, event).get()
            }
        }
    }
}
