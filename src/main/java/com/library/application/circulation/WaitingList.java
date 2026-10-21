package com.library.application.circulation;

import com.library.domain.exception.LibraryException;

import java.util.*;

public class WaitingList {
    private final Map<Long, Deque<Long>> queues = new HashMap<>();

    public int join(long bookId, long memberId){
        Deque<Long> queue = queues.computeIfAbsent(bookId, id -> new ArrayDeque<>());
        if(queue.contains(memberId)){
            throw new LibraryException("Vec ste na listi cekanja za ovu knjigu");
        }
        queue.addLast(memberId);
        return queue.size();
    }

    public Optional<Long> pollNext(long bookId){
        Deque<Long> queue = queues.get(bookId);
        return queue == null ? Optional.empty() : Optional.ofNullable(queue.pollFirst());
    }

    public void remove(long bookId, long memberId) {
        Deque<Long> queue = queues.get(bookId);
        if(queue != null) {
            queue.remove(memberId);
        }
    }

}
