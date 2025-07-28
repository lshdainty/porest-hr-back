package com.lshdainty.myhr.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.IntBinaryOperator;

@Getter
@RequiredArgsConstructor
public enum DuesCalcType {
    PLUS((x, y) -> x + y),
    MINUS((x, y) -> x - y);

    private IntBinaryOperator operator;

    DuesCalcType(IntBinaryOperator operator) {
        this.operator = operator;
    }

    public int applyAsType(int x, int y) {
        return operator.applyAsInt(x, y);
    }
}
