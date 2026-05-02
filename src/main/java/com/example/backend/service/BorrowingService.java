package com.example.backend.service;

import com.example.backend.domain.BorrowingRecord;
import com.example.backend.domain.Inventory;
import com.example.backend.domain.InventoryStatus;
import com.example.backend.dto.borrowing.BorrowingItemResponse;
import com.example.backend.dto.common.PageResponse;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.BorrowingRecordRepository;
import com.example.backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final InventoryRepository inventoryRepository;

    // 查詢未還書的借閱紀錄
    @Transactional(readOnly = true)
    public PageResponse<BorrowingItemResponse> activeBorrowings(Long userId, Pageable pageable) {
        Page<BorrowingItemResponse> page = borrowingRecordRepository.findActiveBorrowingsByUserId(userId, pageable)
                .map(record -> new BorrowingItemResponse(
                        record.getBorrowingId(),
                        record.getInventory().getBook().getIsbn(),
                        record.getInventory().getBook().getName(),
                        record.getInventory().getBook().getAuthor(),
                        record.getBorrowingTime()
                ));
        return PageResponse.from(page);
    }

    @Transactional
    public void returnBook(Long borrowingId, Long userId) {
        BorrowingRecord record = borrowingRecordRepository.findByBorrowingIdAndAccountUserId(borrowingId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "找不到借閱紀錄"));
        if (record.getReturnTime() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "該借閱紀錄已完成還書");
        }

        Inventory inventory = inventoryRepository.findByIdForUpdate(record.getInventory().getInventoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "找不到庫存資料"));
        inventory.setStatus(InventoryStatus.AVAILABLE);
        record.setReturnTime(OffsetDateTime.now());
    }
}
