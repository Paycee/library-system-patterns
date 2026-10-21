package com.library.domain.fee;

public interface LateFeePolicy {
    long calculate(long daysLate);
}
