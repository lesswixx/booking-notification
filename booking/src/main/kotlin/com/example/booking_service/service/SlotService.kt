package com.example.booking_service.service

import com.example.booking_service.model.Slot

interface SlotService {
  fun bookSlot(slotId: Long): Slot
  fun getAvailableSlots(doctorId: Long): List<Slot>
}