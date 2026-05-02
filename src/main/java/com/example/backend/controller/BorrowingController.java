package com.example.backend.controller;

import com.example.backend.dto.borrowing.BorrowingItemResponse;
import com.example.backend.dto.common.PageResponse;
import com.example.backend.security.AppUserPrincipal;
import com.example.backend.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping("/active")
    public PageResponse<BorrowingItemResponse> activeBorrowings(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return borrowingService.activeBorrowings(principal.getUserId(), PageRequest.of(page, size));
    }

    @PostMapping("/{borrowingId}/return")
    public void returnBook(
            @PathVariable Long borrowingId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        borrowingService.returnBook(borrowingId, principal.getUserId());
    }
}
