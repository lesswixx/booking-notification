package com.example.booking_service.service


import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.model.Users
import com.example.booking_service.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun findById(id: Long): Users =
        userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User id=$id not found") }
}
