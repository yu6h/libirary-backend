package com.example.backend.controller;

import com.example.backend.dto.book.BookListItemResponse;
import com.example.backend.dto.common.PageResponse;
import com.example.backend.security.AppUserPrincipal;
import com.example.backend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public PageResponse<BookListItemResponse> listBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.listBooks(page, size);
    }

    @PostMapping("/{isbn}/borrow")
    public void borrow(
            @PathVariable String isbn,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        bookService.borrow(isbn, principal.getUserId());
    }
}
