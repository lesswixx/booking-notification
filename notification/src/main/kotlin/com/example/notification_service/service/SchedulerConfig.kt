package com.example.notification_service.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
class SchedulerConfig {

  @Bean
  fun taskScheduler(): TaskScheduler =
    ThreadPoolTaskScheduler().apply {
      poolSize = 5
      setThreadNamePrefix("notif-sched-")
      initialize()
    }

}
