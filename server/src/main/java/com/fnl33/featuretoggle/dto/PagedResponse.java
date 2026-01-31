package com.fnl33.featuretoggle.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    int page,
    int size
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize()
        );
    }
}
