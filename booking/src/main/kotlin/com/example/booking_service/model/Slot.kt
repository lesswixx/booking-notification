package com.example.booking_service.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "slot")
data class Slot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    val doctor: Doctor,

    @Column(name = "date_time", nullable = false)
    val dateTime: LocalDateTime,

    @Column(nullable = false)
    var booked: Boolean = false
)
