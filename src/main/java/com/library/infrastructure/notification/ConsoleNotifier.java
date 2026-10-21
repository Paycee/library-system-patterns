package com.library.infrastructure.notification;

import com.library.application.circulation.Notifier;

public class ConsoleNotifier implements Notifier {
    @Override
    public void send(long memberId, String message) {
        System.out.println("    [obavestenje -> clan " + memberId + "]" + message);
    }
}
