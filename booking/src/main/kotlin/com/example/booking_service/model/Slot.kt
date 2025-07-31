package com.example.booking_service.model

import com.example.booking_service.model.Doctor
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class Slot(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val dateTime: LocalDateTime,
    var booked: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    val doctor: Doctor
)