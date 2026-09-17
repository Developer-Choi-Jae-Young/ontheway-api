package com.ontheway.repository;

/**
 * {@code like} 패턴 만들기. 사용자가 입력한 {@code %} {@code _} 는 와일드카드가 아니라
 * 문자 그대로 취급한다 — 이스케이프하지 않으면 {@code "%"} 한 글자로 전체 조회가 된다.
 *
 * <p>쿼리 쪽에는 {@code like :param escape '!'} 를 반드시 같이 써야 한다.
 */
final class LikePatterns {

    /** JPQL 의 {@code escape} 절에 들어가는 문자. */
    static final char ESCAPE = '!';

    /** 접두 일치. 주소 검색처럼 앞에서부터 맞추는 필터에 쓴다. */
    static String startsWith(String raw) {
        return raw == null ? null : escape(raw) + "%";
    }

    /** 부분 일치. 물품명처럼 중간이 걸려도 찾아야 하는 필터에 쓴다. */
    static String contains(String raw) {
        return raw == null || raw.isBlank() ? null : "%" + escape(raw) + "%";
    }

    private static String escape(String raw) {
        return raw.replace("" + ESCAPE, "" + ESCAPE + ESCAPE)
                .replace("%", ESCAPE + "%")
                .replace("_", ESCAPE + "_");
    }

    private LikePatterns() {
    }
}
