package com.avocado.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"page", "size", "total_elements", "total_pages", "items"})
public class PageResponse<T> {

    private final int page;

    private final int size;

    @JsonProperty("total_elements")
    private final long totalElements;

    @JsonProperty("total_pages")
    private final long totalPages;


    private final List<T> items;

    // 정적 팩토리 메서드
    public static <T> PageResponse<T> of(
            int page,
            int size,
            long totalElements,
            List<T> items
    ) {
        long totalPages = calculateTotalPages(totalElements, size);

        return new PageResponse<>(
                page,
                size,
                totalElements,
                totalPages,
                items
        );
    }

    // 총 페이지 수 계산
    private static long calculateTotalPages(
            long totalElements,
            int size
    ) {
        return (totalElements + size - 1) / size;
    }
}
