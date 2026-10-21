package com.library.application.circulation;

import java.util.HashMap;
import java.util.Map;

public class Cashier {
    private final Map<Long, Long> debts = new HashMap<>();
    private CirculationMediator mediator;

    void setMediator(CirculationMediator mediator){
        this.mediator = mediator;
    }

    public void charge(long memberId, long amount){
        long total = debts.merge(memberId, amount, Long::sum);
        mediator.debtChanged(memberId, total);
    }

    public long debtOf(long memberId) {
        return debts.getOrDefault(memberId, 0L);
    }

}
