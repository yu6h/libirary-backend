package com.example.backend.repository;

import com.example.backend.domain.Inventory;
import com.example.backend.domain.InventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.book.isbn = :isbn and i.status = :status order by i.inventoryId")
    List<Inventory> findByIsbnAndStatusForUpdate(
            @Param("isbn") String isbn,
            @Param("status") InventoryStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.inventoryId = :inventoryId")
    Optional<Inventory> findByIdForUpdate(@Param("inventoryId") Long inventoryId);
}
