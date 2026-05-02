package com.example.backend.service;

import com.example.backend.domain.Account;
import com.example.backend.domain.Inventory;
import com.example.backend.domain.InventoryStatus;
import com.example.backend.dto.book.BookListItemResponse;
import com.example.backend.dto.common.PageResponse;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.BorrowingRecordRepository;
import com.example.backend.repository.InventoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final EntityManager entityManager;
    private final InventoryRepository inventoryRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Transactional(readOnly = true)
    public PageResponse<BookListItemResponse> listBooks(int page, int size) {
        String jpql = """
                select new com.example.backend.dto.book.BookListItemResponse(
                    b.isbn,
                    b.name,
                    b.author,
                    sum(case when i.status = com.example.backend.domain.InventoryStatus.AVAILABLE then 1 else 0 end)
                )
                from Book b
                left join Inventory i on i.book.isbn = b.isbn
                group by b.isbn, b.name, b.author
                order by b.name asc
                """;
        List<BookListItemResponse> content = entityManager.createQuery(jpql, BookListItemResponse.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        Long total = entityManager.createQuery("select count(b) from Book b", Long.class).getSingleResult();
        Page<BookListItemResponse> paged = new PageImpl<>(content, PageRequest.of(page, size), total);
        return PageResponse.from(paged);
    }

    @Transactional
    public void borrow(String isbn, Long userId) {
        boolean alreadyBorrowed = borrowingRecordRepository
                .existsByAccountUserIdAndInventoryBookIsbnAndReturnTimeIsNull(userId, isbn);
        if (alreadyBorrowed) {
            throw new ApiException(HttpStatus.CONFLICT, "已擁有該書籍，借閱失敗");
        }

        List<Inventory> available = inventoryRepository.findByIsbnAndStatusForUpdate(
                isbn,
                InventoryStatus.AVAILABLE,
                PageRequest.of(0, 1)
        );
        if (available.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "借閱失敗");
        }

        Inventory inventory = available.getFirst();
        inventory.setStatus(InventoryStatus.BORROWED);

        Account accountRef = entityManager.getReference(Account.class, userId);
        BorrowingRecord record = new BorrowingRecord();
        record.setAccount(accountRef);
        record.setInventory(inventory);
        record.setBorrowingTime(OffsetDateTime.now());
        borrowingRecordRepository.save(record);
    }
}
