package com.ontheway.repository;

/**
 * {@code like} 패턴을 만든다. 사용자가 친 {@code %} 와 {@code _} 는 와일드카드가 아니라
 * 그냥 글자로 취급한다. 이스케이프를 안 하면 {@code "%"} 한 글자로 전체 조회가 된다.
 *
 * 주의: 쿼리 쪽에 {@code escape '!'} 를 같이 써야 패턴이 제대로 먹는다.
 */
final class LikePatterns {

    /** JPQL 의 {@code escape} 절에 들어가는 문자. */
    static final char ESCAPE = '!';

    /** 앞에서부터 맞추는 패턴. 주소 검색에 쓴다. */
    static String startsWith(String raw) {
        return raw == null ? null : escape(raw) + "%";
    }

    /** 중간이 걸려도 잡는 패턴. 물품명 검색에 쓴다. */
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
