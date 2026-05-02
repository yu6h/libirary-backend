package com.example.backend.service;

import com.example.backend.dto.book.BookListItemResponse;
import com.example.backend.dto.common.PageResponse;
import com.example.backend.exception.ApiException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final EntityManager entityManager;

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

    /**
     * 借閱改由 PostgreSQL function {@code sp_borrow_book} 執行（見 {@code schema.sql}）。
     * 維護邏輯分散在 DB 與應用層；。
     */
    @Transactional
    public void borrow(String isbn, Long userId) {
        Object row = entityManager.createNativeQuery(
                        "SELECT result_status, borrowing_id FROM sp_borrow_book(?1, ?2)"
                )
                .setParameter(1, userId)
                .setParameter(2, isbn)
                .getSingleResult();

        Object[] cols = (Object[]) row;
        String status = String.valueOf(cols[0]);

        switch (status) {
            case "OK" -> { /* borrowing_id in cols[1]; no return value required */ }
            case "DUPLICATE" -> throw new ApiException(HttpStatus.CONFLICT, "已擁有該書籍，借閱失敗");
            case "NO_STOCK" -> throw new ApiException(HttpStatus.CONFLICT, "借閱失敗");
            default -> throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "借閱處理失敗");
        }
    }
}
