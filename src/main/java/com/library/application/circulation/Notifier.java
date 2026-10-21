package com.library.application.circulation;

@FunctionalInterface
public interface Notifier {
    void send(long memberId,  String message);
}
