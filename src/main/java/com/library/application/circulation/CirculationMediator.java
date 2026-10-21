package com.library.application.circulation;

public interface CirculationMediator {
    void waitRequested(long memberId, long bookId);
    void debtChanged(long memberId, long totalDebt);
    long debtOf(long memberId);
}
