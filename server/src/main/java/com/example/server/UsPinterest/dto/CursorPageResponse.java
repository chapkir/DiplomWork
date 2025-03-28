package com.example.server.UsPinterest.dto;

import java.util.List;

/**
 * Response DTO for cursor-based pagination.
 * Better suited for large datasets than traditional offset-based pagination.
 *
 * @param <T> Type of the data items
 * @param <C> Type of the cursor (typically String or Long)
 */
public class CursorPageResponse<T, C> {
    private List<T> content;
    private C nextCursor;
    private C prevCursor;
    private boolean hasNext;
    private boolean hasPrevious;
    private int pageSize;
    private long totalElements;

    public CursorPageResponse() {
    }

    public CursorPageResponse(List<T> content, C nextCursor, C prevCursor,
                              boolean hasNext, boolean hasPrevious,
                              int pageSize, long totalElements) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.prevCursor = prevCursor;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public C getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(C nextCursor) {
        this.nextCursor = nextCursor;
    }

    public C getPrevCursor() {
        return prevCursor;
    }

    public void setPrevCursor(C prevCursor) {
        this.prevCursor = prevCursor;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
}