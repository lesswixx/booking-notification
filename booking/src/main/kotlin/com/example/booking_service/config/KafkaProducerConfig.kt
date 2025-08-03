package com.example.booking_service.config

import com.example.booking_service.common.AppointmentCreatedEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaProducerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun kafkaAdmin(): KafkaAdmin =
        KafkaAdmin(mapOf(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers))

    @Bean
    fun appointmentsTopic(): NewTopic =
        NewTopic("appointments.created", /*partitions*/ 1, /*replicationFactor*/ 1.toShort())

    @Bean
    fun producerFactory(): ProducerFactory<String, AppointmentCreatedEvent> {
        val props = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG       to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG    to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG  to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG                    to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG      to true
        )
        val factory = DefaultKafkaProducerFactory<String, AppointmentCreatedEvent>(props)
        factory.setTransactionIdPrefix("booking-tx-")
        return factory
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, AppointmentCreatedEvent> =
        KafkaTemplate(producerFactory())
}
