package com.sqlparser.model;

import java.util.Objects;

public class Limit {
    private final int limit;
    private int offset = 0;
    private final int startPosition;

    public Limit(int limit, int startPosition) {
        this.limit = limit;
        this.startPosition = startPosition;
    }

    public Limit(int limit, int offset, int startPosition) {
        this.limit = limit;
        this.offset = offset;
        this.startPosition = startPosition;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "LIMIT " + startPosition +
                " " + limit +
                " " + offset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Limit limit1 = (Limit) o;
        return limit == limit1.limit &&
                offset == limit1.offset &&
                startPosition == limit1.startPosition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, offset, startPosition);
    }
}
