package com.library.domain.fee;

public class StudentFeePolicy implements LateFeePolicy{

    @Override
    public long calculate(long daysLate) {
        return daysLate * 20;
    }
}
