package com.example.booking_service.repository

import com.example.booking_service.model.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Users, Long>