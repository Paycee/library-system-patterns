package com.library.domain.fee;

public class MaxFeeDecorator implements LateFeePolicy{

    private final LateFeePolicy inner;
    private final long maximum;

    public MaxFeeDecorator(LateFeePolicy inner, long maximum) {
        if(maximum < 0){
            throw new IllegalStateException("Maksimum ne moze biti negativan");
        }

        this.inner = inner;
        this.maximum = maximum;
    }


    @Override
    public long calculate(long daysLate) {
        long fee = inner.calculate(daysLate);
        return Math.min(fee, maximum);
    }
}
