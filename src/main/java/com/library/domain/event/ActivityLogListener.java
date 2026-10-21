package com.library.domain.event;

import java.util.HashMap;
import java.util.Map;

public class ActivityLogListener implements LibraryEventListener{

    @Override
    public void onEvent(LibraryEvent event) {
        if(event instanceof LibraryEvent.BookBorrowed b){
            System.out.println("    [log] clan " + b.memberId() + "iznajmio \"" + b.title() + "\", rok " + b.dueDate());
        }else if(event instanceof LibraryEvent.BookReturned r)
        {
            System.out.println("   [log] član " + r.memberId() + " vratio \"" + r.title() + "\"");
        }
    }
}

