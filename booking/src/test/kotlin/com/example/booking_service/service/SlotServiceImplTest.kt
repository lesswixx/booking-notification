package com.example.booking_service.service

import com.example.booking_service.Exception.ResourceNotFoundException
import com.example.booking_service.Exception.SlotAlreadyBookedException
import com.example.booking_service.model.Doctor
import com.example.booking_service.model.Slot
import com.example.booking_service.repository.SlotRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.*
import java.time.LocalDateTime.now
import java.util.*
import kotlin.test.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

internal class SlotServiceImplTest {

    private lateinit var repo: SlotRepository
    private lateinit var svc: SlotServiceImpl

    @BeforeEach fun setUp() {
        repo = mock()
        svc = SlotServiceImpl(repo)
    }

    @Test
    fun getAvailableSlotsDelegatesToRepository() {
        svc.getAvailableSlots(42)
        verify(repo).findByDoctorIdAndBookedFalse(42)
    }

    @Test
    fun bookSlotMarksSlotAsBooked() {
        val slot = Slot(1, Doctor(2, "X", "Y"), dateTime = now(), booked = false)
        whenever(repo.findById(1)).thenReturn(Optional.of(slot))
        whenever(repo.save(any<Slot>())).thenReturn(slot.copy(booked = true))

        val booked = svc.bookSlot(1)
        assertEquals(true, booked.booked)
        verify(repo).save(booked)
    }

    @Test
    fun bookSlotThrowsWhenSlotNotFound() {
        whenever(repo.findById(7)).thenReturn(Optional.empty())
        assertThrows<ResourceNotFoundException> { svc.bookSlot(7) }
    }

    @Test
    fun bookSlotThrowsWhenSlotAlreadyBooked() {
        val slot = Slot(8, Doctor(2, "X", "Y"), dateTime = now(), booked = true)
        whenever(repo.findById(8)).thenReturn(Optional.of(slot))
        assertThrows<SlotAlreadyBookedException> { svc.bookSlot(8) }
    }
}
