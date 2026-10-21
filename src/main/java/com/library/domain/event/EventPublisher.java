package com.library.domain.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventPublisher {

    private final List<LibraryEventListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(LibraryEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(LibraryEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(LibraryEvent event) {
        for(LibraryEventListener listener : listeners)
        {
            try {
                listener.onEvent(event);
            }catch (RuntimeException e)
            {
                System.err.println("    [events] posmatrac nije uspeo: " + e.getMessage());
            }
        }
    }

}
