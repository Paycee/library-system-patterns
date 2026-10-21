package com.library.domain.fee;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LateFeePolicyTest {
    @Test
    void standardnaKaznaJe30PoDanu() {
        assertEquals(150, new StandardFeePolicy().calculate(5));
    }

    @Test
    void bezKasnjenjaNemaKazne() {
        assertEquals(0, new StandardFeePolicy().calculate(0));
    }

    @Test
    void maksimumNeMenjaKaznuIspodGranice() {
        var policy = new MaxFeeDecorator(new StandardFeePolicy(), 500);
        assertEquals(150, policy.calculate(5));       // 5 × 20 = 100 < 500
    }

    @Test
    void maksimumOgranicavaVelikuKaznu() {
        var policy = new MaxFeeDecorator(new StandardFeePolicy(), 500);
        assertEquals(500, policy.calculate(100));     // 100 × 20 = 2000 → 500
    }

    @Test
    void maksimumRadiSaBiloKojomPolitikom() {
        LateFeePolicy desetPoDanu = daysLate -> (int) daysLate * 10;
        var policy = new MaxFeeDecorator(desetPoDanu, 50);
        assertEquals(50, policy.calculate(30));       // 300 → 50
    }

    @Test
    void decoratorSeMozeSlagati() {
        var policy = new MaxFeeDecorator(
                new MaxFeeDecorator(new StandardFeePolicy(), 500), 300);
        assertEquals(300, policy.calculate(100));     // 2000 → 500 → 300
    }
}
