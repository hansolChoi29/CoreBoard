package com.example.coreboard.domain.common.response;

public class SliceInfo {
    private final int size;
    private final int numberOfElement;
    private final boolean hasNext;

    public SliceInfo(
            int size,
            int numberOfElement,
            boolean hasNext
    ) {
        this.size = size;
        this.numberOfElement = numberOfElement;
        this.hasNext = hasNext;
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfElement() {
        return numberOfElement;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
