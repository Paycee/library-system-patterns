package com.library.domain.fee;

public class NoFeePolicy implements LateFeePolicy{

    @Override
    public long calculate(long daysLate) {
        return 0;
    }
}
