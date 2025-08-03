package com.example.notification_service.config

import com.example.notification_service.model.AppointmentCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}") private val groupId: String
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, AppointmentCreatedEvent> {
        val json = JsonDeserializer(AppointmentCreatedEvent::class.java).apply {
            addTrustedPackages("*")
            setUseTypeHeaders(false)
        }

        val props = mapOf(
            BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            GROUP_ID_CONFIG to groupId,
            AUTO_OFFSET_RESET_CONFIG to "earliest"
        )

        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            json
        )
    }

    @Bean(name = ["kafkaListenerContainerFactory"])
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, AppointmentCreatedEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, AppointmentCreatedEvent> =
      ConcurrentKafkaListenerContainerFactory<String, AppointmentCreatedEvent>().apply {
          setConsumerFactory(consumerFactory)
          setConcurrency(1)
      }
}
