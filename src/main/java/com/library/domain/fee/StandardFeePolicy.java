package com.library.domain.fee;

public class StandardFeePolicy implements LateFeePolicy{

    @Override
    public long calculate(long daysLate) {
        return daysLate * 30;
    }
}
