package com.ontheway.global.util;

import java.security.SecureRandom;

public class TempPasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;
    private static final int LENGTH = 8;

    public static String generate() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(DIGIT.charAt(random.nextInt(DIGIT.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = sb.length(); i < LENGTH; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        for (int i = sb.length() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = sb.charAt(i);
            sb.setCharAt(i, sb.charAt(j));
            sb.setCharAt(j, temp);
        }

        return sb.toString();
    }
}
