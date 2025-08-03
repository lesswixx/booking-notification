package com.example.booking_service.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class Users(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long =0,
    val name: String,
    val email: String
)