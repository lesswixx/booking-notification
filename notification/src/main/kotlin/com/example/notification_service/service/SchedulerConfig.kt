package com.example.notification_service.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class SchedulerConfig {

    @Bean
    fun taskScheduler(): TaskScheduler {
        val tpts = ThreadPoolTaskScheduler()
        tpts.setPoolSize(5)
        tpts.setThreadNamePrefix("notif-sched-")
        tpts.initialize()
        return tpts
    }
}
