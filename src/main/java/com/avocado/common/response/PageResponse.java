package com.avocado.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.util.List;

@Getter
@JsonPropertyOrder({"page", "size", "total_elements", "total_pages", "has_next", "items"})
public class PageResponse<T> {

    private final int page;

    private final int size;

    @JsonProperty("total_elements")
    private final long totalElements;

    @JsonProperty("total_pages")
    private final long totalPages;

    @JsonProperty("has_next")
    private final boolean hasNext;

    private final List<T> items;

    // 외부에서 생성자 호출 제한
    private PageResponse(
            int page,
            int size,
            long totalElements,
            long totalPages,
            boolean hasNext,
            List<T> items
    ) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.items = items;
    }

    // 정적 팩토리 메서드
    public static <T> PageResponse<T> of(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            List<T> items
    ) {
        return new PageResponse<>(
                page,
                size,
                totalElements,
                totalPages,
                hasNext,
                items
        );
    }
}
