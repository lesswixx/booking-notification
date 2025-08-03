package com.example.booking_service.controller

import com.example.booking_service.Exception.ApiError
import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, req: HttpServletRequest): ResponseEntity<ApiError> {
        val error = ApiError(
            status   = NOT_FOUND.value(),
            error    = NOT_FOUND.reasonPhrase,
            message  = ex.message ?: "Resource not found",
            path     = req.requestURI
        )
        return ResponseEntity(error, NOT_FOUND)
    }

    @ExceptionHandler(SlotAlreadyBookedException::class, DataIntegrityViolationException::class)
    fun handleConflict(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val error = ApiError(
            status   = CONFLICT.value(),
            error    = CONFLICT.reasonPhrase,
            message  = ex.message ?: "Slot already booked",
            path     = req.requestURI
        )
        return ResponseEntity(error, CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, req: HttpServletRequest): ResponseEntity<ApiError> {
        val error = ApiError(
            status   = INTERNAL_SERVER_ERROR.value(),
            error    = INTERNAL_SERVER_ERROR.reasonPhrase,
            message  = ex.message ?: "Internal server error",
            path     = req.requestURI
        )
        return ResponseEntity(error, INTERNAL_SERVER_ERROR)
    }
}
