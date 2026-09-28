package com.ontheway.global.util;

import java.math.BigDecimal;

public class CoordinateValidator {

    public static boolean within(BigDecimal value, int limit) {
        return value.abs().compareTo(BigDecimal.valueOf(limit)) <= 0;
    }
}
