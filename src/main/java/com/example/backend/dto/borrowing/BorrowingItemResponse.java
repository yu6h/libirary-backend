package com.example.backend.dto.borrowing;

import java.time.OffsetDateTime;

public record BorrowingItemResponse(
        Long borrowingId,
        String isbn,
        String bookName,
        String author,
        OffsetDateTime borrowingTime
) {
}
