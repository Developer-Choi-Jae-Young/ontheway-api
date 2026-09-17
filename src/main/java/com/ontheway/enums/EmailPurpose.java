package com.ontheway.enums;

/**
 * 이메일 인증 코드의 용도.
 *
 * <p>인증 코드는 RDB가 아니라 인메모리에 보관하며, 저장 키는 반드시 {@code email + purpose} 복합이다.
 * email 만으로 잡으면 용도별 코드가 서로 덮어쓰고, A 용도로 받은 코드로 B 용도를 통과시킬 수 있다
 * (ERD_REVIEW 1-3 / 1-7).
 */
public enum EmailPurpose {
    SIGN_UP,
    FIND_ID,
    FIND_PASSWORD,
    CHANGE_EMAIL
}
