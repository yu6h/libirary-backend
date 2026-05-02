package com.example.backend.dto.book;

public record BookListItemResponse(
        String isbn,
        String name,
        String author,
        long availableCount
) {
}
