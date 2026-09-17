package com.ontheway.enums;

/**
 * 이메일 인증 코드의 용도. 코드는 DB 가 아니라 메모리에 들고 있는다.
 *
 * 주의: 저장 키는 {@code email + purpose} 여야 한다. email 만으로 잡으면 용도가 다른 코드가
 * 서로 덮어쓰고, 가입용으로 받은 코드로 비밀번호 찾기를 통과할 수 있다.
 */
public enum EmailPurpose {
    SIGN_UP,
    FIND_ID,
    FIND_PASSWORD,
    CHANGE_EMAIL
}
