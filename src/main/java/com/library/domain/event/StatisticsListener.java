package com.library.domain.event;

import java.util.HashMap;
import java.util.Map;

public class StatisticsListener implements LibraryEventListener{

    private final Map<String, Integer> borrowCount = new HashMap<>();

    @Override
    public void onEvent(LibraryEvent event) {
        if(event instanceof LibraryEvent.BookBorrowed b)
        {
            borrowCount.merge(b.title(), 1, Integer::sum);
        }
    }

    public Map<String, Integer> getBorrowCount()
    {
        return Map.copyOf(borrowCount);
    }

}
