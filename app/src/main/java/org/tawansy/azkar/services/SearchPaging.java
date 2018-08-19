package org.tawansy.azkar.services;

import java.util.Formatter;

public class SearchPaging {
    private int currentSearchPagesCount;
    private final int pageLength = 50;

    public void init(int totalHitsCount) {
        currentSearchPagesCount = (int) Math.ceil((double) totalHitsCount / (double) pageLength);
    }

    public int getPageLength() {
        return pageLength;
    }

    public int getNextSearchPageNumber(int currentSearchPageNumber) {
        int newSearchPageNumber = currentSearchPageNumber + 1;
        if (newSearchPageNumber > currentSearchPagesCount) {
            newSearchPageNumber--;
        }
        return newSearchPageNumber;
    }

    public int getPreviousPageNumber(int currentSearchPageNumber) {
        int newPageNumber = currentSearchPageNumber - 1;
        if (newPageNumber < 1) {
            newPageNumber = 1;
        }
        return newPageNumber;
    }

    public String getPagingString(int currentSearchPageNumber) {
        return new Formatter().format(" ( %d / %d ) ", currentSearchPageNumber, currentSearchPagesCount).toString();
    }
}
