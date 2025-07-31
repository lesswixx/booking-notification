package com.example.booking_service.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "appointment")
data class Appointment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val users: Users,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    val doctor: Doctor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    val slot: Slot,

    @Column(name = "date_time", nullable = false)
    val dateTime: LocalDateTime
)
