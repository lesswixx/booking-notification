package com.example.booking_service.service

import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.model.Slot
import com.example.booking_service.repository.SlotRepository
import org.springframework.stereotype.Service

@Service
class SlotServiceImpl(
  private val slotRepository: SlotRepository
) : SlotService {

  override fun bookSlot(slotId: Long): Slot {
    val slot: Slot = slotRepository.findById(slotId)
      .orElseThrow { ResourceNotFoundException("Slot id=$slotId not found") }

    if (slot.booked) {
      throw SlotAlreadyBookedException("Slot id=${slot.id} already booked")
    }

    slot.booked = true
    return slotRepository.save(slot)
  }

  override fun getAvailableSlots(doctorId: Long): List<Slot> =
    slotRepository.findByDoctorIdAndBookedFalse(doctorId)
}
