package com.example.bank.common.util;

import com.example.bank.common.constant.ApiConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

public final class PageableUtil {

    private PageableUtil() {
    }

    public static Pageable create(
            Integer page,
            Integer size,
            String sortBy,
            String direction) {

        int pageNumber = normalizePage(page);
        int pageSize = normalizePageSize(size);

        String property = normalizeSortProperty(sortBy);
        Sort.Direction sortDirection =
                normalizeDirection(direction);

        return PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(sortDirection, property)
        );
    }

    public static Pageable create(
            Integer page,
            Integer size,
            String sortBy) {

        return create(
                page,
                size,
                sortBy,
                ApiConstants.DEFAULT_SORT_DIRECTION
        );
    }

    public static Pageable create(
            Integer page,
            Integer size) {

        return create(
                page,
                size,
                "id",
                ApiConstants.DEFAULT_SORT_DIRECTION
        );
    }

    private static int normalizePage(Integer page) {

        if (page == null || page < 0) {
            return ApiConstants.DEFAULT_PAGE_NUMBER;
        }

        return page;
    }

    private static int normalizePageSize(Integer size) {

        if (size == null || size <= 0) {
            return ApiConstants.DEFAULT_PAGE_SIZE;
        }

        return Math.min(
                size,
                ApiConstants.MAX_PAGE_SIZE
        );
    }

    private static String normalizeSortProperty(
            String sortBy) {

        if (sortBy == null
                || sortBy.isBlank()) {

            return "id";
        }

        return sortBy.trim();
    }

    private static Sort.Direction normalizeDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }

        try {
            return Sort.Direction.fromString(direction.trim());
        } catch (IllegalArgumentException e) {
            return Sort.Direction.ASC;
        }
    }

}