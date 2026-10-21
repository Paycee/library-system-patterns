package com.library.domain.event;

@FunctionalInterface
public interface LibraryEventListener {
    void onEvent(LibraryEvent event);
}
