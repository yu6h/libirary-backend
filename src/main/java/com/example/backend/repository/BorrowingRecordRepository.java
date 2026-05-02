package com.example.backend.repository;

import com.example.backend.domain.BorrowingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    boolean existsByAccountUserIdAndInventoryBookIsbnAndReturnTimeIsNull(Long userId, String isbn);

    @Query("""
            select br
            from BorrowingRecord br
            where br.account.userId = :userId
              and br.returnTime is null
            order by br.borrowingTime desc
            """)
    Page<BorrowingRecord> findActiveBorrowingsByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<BorrowingRecord> findByBorrowingIdAndAccountUserId(Long borrowingId, Long userId);
}
