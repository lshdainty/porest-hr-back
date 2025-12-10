package com.lshdainty.porest.dues.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.LongBinaryOperator;

@Getter
@RequiredArgsConstructor
public enum DuesCalcType {
    PLUS((x, y) -> x + y),
    MINUS((x, y) -> x - y);

    private LongBinaryOperator operator;

    DuesCalcType(LongBinaryOperator operator) {
        this.operator = operator;
    }

    public long applyAsType(long x, long y) {
        return operator.applyAsLong(x, y);
    }
}
