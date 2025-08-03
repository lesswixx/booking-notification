package com.example.booking_service.repository

import com.example.booking_service.model.Slot
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface SlotRepository : JpaRepository<Slot, Long> {



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Slot>

    fun findByDoctorIdAndBookedFalse(doctorId: Long): List<Slot>


}
