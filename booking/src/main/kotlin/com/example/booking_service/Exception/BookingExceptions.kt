package com.example.booking_service.Exception

class ResourceNotFoundException(message: String) : RuntimeException(message)
class SlotAlreadyBookedException(message: String) : RuntimeException(message)